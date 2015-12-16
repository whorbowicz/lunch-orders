package com.horbowicz.lunch.orders.domain.order

import java.time.LocalDateTime

import com.horbowicz.lunch.orders.BaseSpec
import com.horbowicz.lunch.orders.command.order.item.AddOrderItem
import com.horbowicz.lunch.orders.common.TimeProvider
import com.horbowicz.lunch.orders.domain.IdProvider
import com.horbowicz.lunch.orders.event.EventPublisher
import com.horbowicz.lunch.orders.event.order.item.OrderItemAdded

import scalaz.Scalaz._

class OrderAggregateSpec extends BaseSpec
{
  val orderId = "123"
  val idProvider = mock[IdProvider]
  val timeProvider = mock[TimeProvider]
  val eventPublisher = mock[EventPublisher]
  val order = new OrderAggregate(
    orderId,
    idProvider,
    timeProvider,
    eventPublisher)

  val orderRepository = mock[OrderRepository]
  val sampleCommand = AddOrderItem(
    orderId,
    orderingPerson = "WHO",
    description = "Cheeseburger with chips and diet Coke",
    price = BigDecimal("15.99"))

  "Order" - {
    "returns Id of newly added item and publishes OrderItemAdded event" in {
      val expectedId = "12345"
      val currentDateTime = LocalDateTime.now()
      idProvider.get _ expects() returning expectedId
      timeProvider.getCurrentDateTime _ expects() returning currentDateTime
      eventPublisher.publish _ expects OrderItemAdded(
        expectedId,
        currentDateTime,
        sampleCommand.orderId,
        sampleCommand.orderingPerson,
        sampleCommand.description,
        sampleCommand.price)
      order.addItem(
        sampleCommand,
        response => response shouldBe expectedId.right)
    }

    "returns Invalid order id error " +
      "if command's order id does not match it's own id" in {
      order.addItem(
        sampleCommand.copy(orderId = "456"),
        response => response shouldBe InvalidOrderId.left)
    }
  }
}
