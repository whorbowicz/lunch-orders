package com.horbowicz.lunch.orders.domain.order

import java.time._

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.TestProbe
import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders._
import com.horbowicz.lunch.orders.command.order.{OpenOrder, OrderCommand}
import com.horbowicz.lunch.orders.common.TimeProvider
import com.horbowicz.lunch.orders.domain.IdProvider
import com.horbowicz.lunch.orders.domain.order.error.{ImpossibleDeliveryTime, OrderNotFound}
import com.horbowicz.lunch.orders.event.order.OrderOpened

import scala.language.postfixOps
import scalaz.Scalaz._

class OrdersSpec
  extends BaseActorSpec(ActorSystem("OrdersSpec"))
    with EventsListener {

  override def persistenceId: String = Orders.PersistenceId

  private val idProvider = mock[IdProvider]
  private val timeProvider = mock[TimeProvider]

  private val openOrder = OpenOrder(
    provider = "Food House",
    personResponsible = "WHO",
    orderingTime = LocalTime.of(10, 30),
    expectedDeliveryTime = LocalTime.of(12, 30))

  private var orders: ActorRef = _
  private var orderProbe: TestProbe = _
  private var probeId: Id = _

  class TestOrders extends Orders(idProvider, timeProvider) {

    override def createAggregate(id: Global.Id): ActorRef = {
      orderProbe = TestProbe()
      probeId = id
      orderProbe.ref
    }
  }

  before {
    orders = system.actorOf(Props(new TestOrders()))
  }

  "Orders" - {
    "on OpenOrder command" - {
      "returns error if expected delivery time is before ordering time" in {
        val command = openOrder.copy(
          expectedDeliveryTime = openOrder.orderingTime.minusHours(1))

        within(defaultDuration) {
          orders ! command
          expectMsg(ImpossibleDeliveryTime.left)
        }
        eventsListener.within(defaultDuration) {
          eventsListener.expectNoMsg()
        }
      }

      "returns error if expected delivery time is same as ordering time" in {
        val command = openOrder.copy(
          expectedDeliveryTime = openOrder.orderingTime)

        within(defaultDuration) {
          orders ! command
          expectMsg(ImpossibleDeliveryTime.left)
        }
        eventsListener.within(defaultDuration) {
          eventsListener.expectNoMsg()
        }
      }

      "returns Id of successfully opened order and publishes OrderOpened event" in
        {
          val expectedId: String = "12345"
          val currentDateTime = LocalDateTime.now()
          idProvider.get _ expects() returning expectedId
          timeProvider.getCurrentDateTime _ expects() returning currentDateTime

          val orderOpenedEvent = OrderOpened(
            expectedId,
            currentDateTime,
            openOrder.provider,
            openOrder.personResponsible,
            openOrder.orderingTime,
            openOrder.expectedDeliveryTime)

          within(defaultDuration) {
            orders ! openOrder
            expectMsg(expectedId.right)
          }
          eventsListener.within(defaultDuration) {
            eventsListener.expectMsg(orderOpenedEvent)
          }
      }
    }

    "on OrderCommand" - {
      "returns order not found if order with given id does not exist" in {
        within(defaultDuration) {
          val expectedId = "12345"
          orders ! new OrderCommand[Any] {
            override def orderId = expectedId
          }
          expectMsg(OrderNotFound(expectedId).left)
        }
        eventsListener.within(defaultDuration) {
          eventsListener.expectNoMsg()
        }
      }

      "forwards command to Order with given Id if it exists" in {
        val expectedId = "12345"
        val currentDateTime = LocalDateTime.now()
        idProvider.get _ expects() returning expectedId
        timeProvider.getCurrentDateTime _ expects() returning currentDateTime

        val orderOpenedEvent = OrderOpened(
          expectedId,
          currentDateTime,
          openOrder.provider,
          openOrder.personResponsible,
          openOrder.orderingTime,
          openOrder.expectedDeliveryTime)

        val command = new OrderCommand[Any] {
          override def orderId = expectedId
        }

        within(defaultDuration) {
          orders ! openOrder
          expectMsg(expectedId.right)
          orders ! command
          orderProbe.expectMsg(command)
        }
        eventsListener.within(defaultDuration) {
          eventsListener.expectMsg(orderOpenedEvent)
        }
        probeId mustBe expectedId
      }
    }
  }
}
