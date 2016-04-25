package com.horbowicz.lunch.orders.domain.order

import java.time.LocalDateTime

import akka.actor.{ActorRef, ActorSystem}
import com.horbowicz.lunch.orders._
import com.horbowicz.lunch.orders.command.order.{AddOrderItem, PlaceOrder}
import com.horbowicz.lunch.orders.common.TimeProvider
import com.horbowicz.lunch.orders.domain.IdProvider
import com.horbowicz.lunch.orders.domain.order.error._
import com.horbowicz.lunch.orders.event.order.{OrderItemAdded, OrderPlaced}

import scalaz.Scalaz._

class OrderAggregateSpec
  extends BaseActorSpec(ActorSystem("OrderAggregateSpec"))
    with EventsListener {

  private val orderId = "123"

  private val idProvider = mock[IdProvider]
  private val timeProvider = mock[TimeProvider]
  private val addItem = AddOrderItem(
    orderId,
    orderingPerson = "WHO",
    description = "Cheeseburger with chips and diet Coke",
    price = BigDecimal("15.99"))
  private val placeOrderCommand = PlaceOrder(
    orderId,
    personResponsible = "WHO")

  override def persistenceId = s"order-$orderId"

  private var orderAggregate: ActorRef = _

  before {
    orderAggregate = system
      .actorOf(OrderAggregate.props(orderId, idProvider, timeProvider))
  }

  "Order" - {
    "returns Id of newly added item and publishes OrderItemAdded event" in {
      val expectedId = "12345"
      val currentDateTime = LocalDateTime.now()
      idProvider.get _ expects() returning expectedId
      timeProvider.getCurrentDateTime _ expects() returning currentDateTime
      val orderItemAdded = OrderItemAdded(
        expectedId,
        currentDateTime,
        addItem.orderId,
        addItem.orderingPerson,
        addItem.description,
        addItem.price)

      within(defaultDuration) {
        orderAggregate ! addItem
        expectMsg(expectedId.right)
      }
      eventsListener.within(defaultDuration) {
        eventsListener.expectMsg(orderItemAdded)
      }
    }

    "returns Invalid order id error " +
      "if add item command's order id does not match it's own id" in {
      within(defaultDuration) {
        orderAggregate ! addItem.copy(orderId = "456")
        expectMsg(InvalidOrderId.left)
      }
      eventsListener.within(defaultDuration) {
        eventsListener.expectNoMsg()
      }
    }

    "returns Invalid order id error " +
      "if place order command's order id does not match it's own id" in {
      within(defaultDuration) {
        orderAggregate ! placeOrderCommand.copy(orderId = "456")
        expectMsg(InvalidOrderId.left)
      }
      eventsListener.within(defaultDuration) {
        eventsListener.expectNoMsg()
      }
    }

    "returns Unfilled order error " +
      "if no items were added to order before attempting to place it" in {
      within(defaultDuration) {
        orderAggregate ! placeOrderCommand
        expectMsg(UnfilledOrder.left)
      }
      eventsListener.within(defaultDuration) {
        eventsListener.expectNoMsg()
      }
    }

    "returns unit and publishes OrderPlaced event when placed successfully" in {

      val expectedId = "12345"
      idProvider.get _ expects() returning expectedId
      val currentDateTime = LocalDateTime.now()
      timeProvider.getCurrentDateTime _ expects() returning
        currentDateTime twice()
      val orderPlaced = OrderPlaced(
        orderId,
        currentDateTime,
        placeOrderCommand.personResponsible)

      within(defaultDuration) {
        orderAggregate ! addItem
        expectMsg(expectedId.right)
        orderAggregate ! placeOrderCommand
        expectMsg(().right)
      }
      eventsListener.within(defaultDuration) {
        eventsListener.expectMsgClass(classOf[OrderItemAdded])
        eventsListener.expectMsg(orderPlaced)
      }
    }
  }
}
