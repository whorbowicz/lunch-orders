package com.horbowicz.lunch.orders.domain.order

import java.time._

import akka.actor.{ActorRef, ActorSystem}
import com.horbowicz.lunch.orders.command.order.OpenOrder
import com.horbowicz.lunch.orders.common.TimeProvider
import com.horbowicz.lunch.orders.domain.IdProvider
import com.horbowicz.lunch.orders.domain.order.OrdersActor.{FindOrder, OrderFound}
import com.horbowicz.lunch.orders.domain.order.error.{ImpossibleDeliveryTime, OrderNotFound}
import com.horbowicz.lunch.orders.event.order.OrderOpened
import com.horbowicz.lunch.orders.{BaseActorSpec, EventsListener}

import scala.language.postfixOps
import scalaz.Scalaz._
import scalaz._

class OpenOrderHandlerSpec
  extends BaseActorSpec(ActorSystem("OpenOrderHandlerSpec"))
    with EventsListener {

  override def persistenceId: String = OpenOrderHandler.PersistenceId

  private val idProvider = mock[IdProvider]
  private val timeProvider = mock[TimeProvider]

  private val openOrder = OpenOrder(
    provider = "Food House",
    personResponsible = "WHO",
    orderingTime = LocalTime.of(10, 30),
    expectedDeliveryTime = LocalTime.of(12, 30))

  private var handler: ActorRef = _

  before {
    handler = system
      .actorOf(
        OpenOrderHandler.props(idProvider, timeProvider))
  }

  "Open order handler" - {
    "publishes OrderOpened event and returns Id of newly opened order" in {
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
        handler ! openOrder
        expectMsg(expectedId.right)
      }
      eventsListener.within(defaultDuration) {
        eventsListener.expectMsg(orderOpenedEvent)
      }
    }

    "returns error if expected delivery time is before ordering time" in {
      val command = openOrder.copy(
        expectedDeliveryTime = openOrder.orderingTime.minusHours(1))

      within(defaultDuration) {
        handler ! command
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
        handler ! command
        expectMsg(ImpossibleDeliveryTime.left)
      }
      eventsListener.within(defaultDuration) {
        eventsListener.expectNoMsg()
      }
    }

    "returns order not found if requested order does not exist" in {
      within(defaultDuration) {
        val orderId = "12345"
        handler ! FindOrder(orderId)
        expectMsg(OrderNotFound(orderId).left)
      }
      eventsListener.within(defaultDuration) {
        eventsListener.expectNoMsg()
      }
    }

    "returns order if requested order exists" in {
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
        handler ! openOrder
        expectMsg(expectedId.right)
        handler ! FindOrder(expectedId)
        val \/-(OrderFound(orderId, _)) = expectMsgType[OrderNotFound \/
          OrderFound]
        orderId mustBe expectedId
      }
      eventsListener.within(defaultDuration) {
        eventsListener.expectMsg(orderOpenedEvent)
      }
    }
  }
}
