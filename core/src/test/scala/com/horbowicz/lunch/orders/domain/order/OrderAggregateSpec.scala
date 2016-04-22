package com.horbowicz.lunch.orders.domain.order

import java.time.LocalDateTime

import akka.actor.{ActorRef, ActorSystem}
import com.horbowicz.lunch.orders._
import com.horbowicz.lunch.orders.command.order.{AddOrderItem, PlaceOrder}
import com.horbowicz.lunch.orders.common.TimeProvider
import com.horbowicz.lunch.orders.common.callback._
import com.horbowicz.lunch.orders.domain.IdProvider
import com.horbowicz.lunch.orders.domain.order.error._
import com.horbowicz.lunch.orders.event.EventPublisher
import com.horbowicz.lunch.orders.event.order.{OrderItemAdded, OrderPlaced}

import scalaz.Scalaz._

class OrderAggregateSpec
  extends BaseActorSpec(ActorSystem("OrderAggregateSpec"))
    with EventsListener {

  private val orderId = "123"

  private val idProvider = mock[IdProvider]
  private val timeProvider = mock[TimeProvider]
  private val eventPublisher = mock[EventPublisher]
  private val addItem = AddOrderItem(
    orderId,
    orderingPerson = "WHO",
    description = "Cheeseburger with chips and diet Coke",
    price = BigDecimal("15.99"))
  private val placeOrderCommand = PlaceOrder(
    orderId,
    personResponsible = "WHO")

  private var order: OrderAggregate = _

  override def persistenceId = s"order-$orderId"

  private var orderAggregate: ActorRef = _

  before {
    order = new OrderAggregate(
      orderId,
      idProvider,
      timeProvider,
      eventPublisher)
    orderAggregate = system
      .actorOf(OrderAggregateActor.props(orderId, idProvider, timeProvider))
  }

  "Order" - {
    "returns Id of newly added item and publishes OrderItemAdded event" in {
      val expectedId = "12345"
      val currentDateTime = LocalDateTime.now()
      idProvider.get _ expects() returning expectedId twice()
      timeProvider.getCurrentDateTime _ expects() returning
        currentDateTime twice()
      val orderItemAdded = OrderItemAdded(
        expectedId,
        currentDateTime,
        addItem.orderId,
        addItem.orderingPerson,
        addItem.description,
        addItem.price)
      eventPublisher.publish[OrderItemAdded] _ expects orderItemAdded returning
        orderItemAdded.point[CallbackHandler]
      order.addItem(addItem) {
        response => response mustBe expectedId.right
      }
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
      order.addItem(addItem.copy(orderId = "456")) {
        response => response mustBe InvalidOrderId.left
      }
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
      order.place(placeOrderCommand.copy(orderId = "456")) {
        response => response mustBe InvalidOrderId.left
      }
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
      order.place(placeOrderCommand) {
        response => response mustBe UnfilledOrder.left
      }
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
      order.applyEvent(
        OrderItemAdded(
          expectedId,
          LocalDateTime.now(),
          orderId,
          addItem.orderingPerson,
          addItem.description,
          addItem.price))
      val currentDateTime = LocalDateTime.now()
      timeProvider.getCurrentDateTime _ expects() returning
        currentDateTime repeat 3
      val orderPlaced = OrderPlaced(
        orderId,
        currentDateTime,
        placeOrderCommand.personResponsible)
      eventPublisher.publish[OrderPlaced] _ expects orderPlaced returning
        orderPlaced.point[CallbackHandler]
      order.place(placeOrderCommand) {
        response => response mustBe ().right
      }

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
