package com.horbowicz.lunch.orders.domain.order

import java.time.{LocalDateTime, LocalTime}

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.TestProbe
import com.horbowicz.lunch.orders._
import com.horbowicz.lunch.orders.command.order.AddOrderItem
import com.horbowicz.lunch.orders.common.TimeProvider
import com.horbowicz.lunch.orders.domain.IdProvider
import com.horbowicz.lunch.orders.domain.order.OrdersActor.{FindOrder, OrderFound}
import com.horbowicz.lunch.orders.domain.order.error.OrderNotFound
import com.horbowicz.lunch.orders.event.order.OrderOpened

import scala.language.postfixOps
import scalaz.Scalaz._
import scalaz.{\/, \/-}

class AddOrderItemHandlerSpec
  extends BaseActorSpec(ActorSystem("AddOrderItemHandlerSpec")) {

  private val idProvider = mock[IdProvider]
  private val timeProvider = mock[TimeProvider]
  private val orderId = "123"
  private val addOrderItem = AddOrderItem(
    orderId,
    orderingPerson = "WHO",
    description = "Cheeseburger with chips and diet Coke",
    price = BigDecimal("15.99"))
  private var handler: ActorRef = _
  private val orderProbe = TestProbe(s"order-$orderId")
  val orderOpenedEvent = OrderOpened(
    orderId,
    LocalDateTime.now(),
    provider = "Food House",
    personResponsible = "WHO",
    orderingTime = LocalTime.of(10, 30),
    expectedDeliveryTime = LocalTime.of(12, 30))

  class TestAddOrderItemHandler
    extends AddOrderItemHandler(idProvider, timeProvider) {

    override def createAggregate(id: Global.Id): ActorRef = orderProbe.ref
  }

  before {
    handler = system.actorOf(Props(new TestAddOrderItemHandler()))
  }

  "Add order item handler" - {
    "returns Order not found error if order with given Id cannot be found" in {
      within(defaultDuration) {
        handler ! addOrderItem
        expectMsg(OrderNotFound(orderId).left)
      }
    }

    "passes command to Order with given Id if it was found " +
      "and returns Order's response back" in {
      val expectedId = "12345"

      within(defaultDuration) {
        handler ! orderOpenedEvent
        handler ! addOrderItem
        orderProbe.expectMsg(addOrderItem)
        orderProbe.reply(expectedId.right)
        expectMsg(expectedId.right)
      }
    }

    "returns order not found if requested order does not exist" in {
      within(defaultDuration) {
        handler ! FindOrder(orderId)
        expectMsg(OrderNotFound(orderId).left)
      }
    }

    "returns order if requested order exists" in {
      within(defaultDuration) {
        handler ! orderOpenedEvent
        handler ! FindOrder(orderId)
        val \/-(OrderFound(id, _)) = expectMsgType[OrderNotFound \/ OrderFound]
        id mustBe orderId
      }
    }
  }
}
