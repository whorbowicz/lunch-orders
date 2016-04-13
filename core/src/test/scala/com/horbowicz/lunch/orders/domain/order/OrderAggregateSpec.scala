package com.horbowicz.lunch.orders.domain.order

import java.time.LocalDateTime

import com.horbowicz.lunch.orders.BaseSpec
import com.horbowicz.lunch.orders.command.order.{AddOrderItem, PlaceOrder}
import com.horbowicz.lunch.orders.common.TimeProvider
import com.horbowicz.lunch.orders.common.callback._
import com.horbowicz.lunch.orders.domain.IdProvider
import com.horbowicz.lunch.orders.domain.order.error._
import com.horbowicz.lunch.orders.event.EventPublisher
import com.horbowicz.lunch.orders.event.order.{OrderItemAdded, OrderPlaced}

import scalaz.Scalaz._

class OrderAggregateSpec extends BaseSpec {

  private val orderId = "123"
  private val idProvider = mock[IdProvider]
  private val timeProvider = mock[TimeProvider]
  private val eventPublisher = mock[EventPublisher]
  private val addItemCommand = AddOrderItem(
    orderId,
    orderingPerson = "WHO",
    description = "Cheeseburger with chips and diet Coke",
    price = BigDecimal("15.99"))
  private val placeOrderCommand = PlaceOrder(
    orderId,
    personResponsible = "WHO")

  private var order: OrderAggregate = _

  before {
    order = new OrderAggregate(
      orderId,
      idProvider,
      timeProvider,
      eventPublisher)
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
        addItemCommand.orderId,
        addItemCommand.orderingPerson,
        addItemCommand.description,
        addItemCommand.price)
      eventPublisher.publish[OrderItemAdded] _ expects orderItemAdded returning
        orderItemAdded.point[CallbackHandler]
      order.addItem(addItemCommand) {
        response => response mustBe expectedId.right
      }
    }

    "returns Invalid order id error " +
      "if add item command's order id does not match it's own id" in {
      order.addItem(addItemCommand.copy(orderId = "456")) {
        response => response mustBe InvalidOrderId.left
      }
    }

    "returns Invalid order id error " +
      "if place order command's order id does not match it's own id" in {
      order.place(placeOrderCommand.copy(orderId = "456")) {
        response => response mustBe InvalidOrderId.left
      }
    }

    "returns Unfilled order error " +
      "if no items were added to order before attempting to place it" in {
      order.place(placeOrderCommand) {
        response => response mustBe UnfilledOrder.left
      }
    }

    "returns unit and publishes OrderPlaced event when placed successfully" in {
      order.applyEvent(
        OrderItemAdded(
          "12345",
          LocalDateTime.now(),
          orderId,
          addItemCommand.orderingPerson,
          addItemCommand.description,
          addItemCommand.price))
      val currentDateTime = LocalDateTime.now()
      timeProvider.getCurrentDateTime _ expects() returning currentDateTime
      val orderPlaced = OrderPlaced(
        orderId,
        currentDateTime,
        placeOrderCommand.personResponsible)
      eventPublisher.publish[OrderPlaced] _ expects orderPlaced returning
        orderPlaced.point[CallbackHandler]
      order.place(placeOrderCommand) {
        response => response mustBe ().right
      }
    }
  }
}
