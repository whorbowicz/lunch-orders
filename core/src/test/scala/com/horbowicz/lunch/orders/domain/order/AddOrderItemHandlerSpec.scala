package com.horbowicz.lunch.orders.domain.order

import com.horbowicz.lunch.orders.command.order.item.AddOrderItem
import com.horbowicz.lunch.orders.{BaseSpec, domain}

import scalaz.Scalaz._
import scalaz._

class AddOrderItemHandlerSpec extends BaseSpec
{
  val orderId = "123"
  val order = mock[domain.Order]
  val orderRepository = mock[OrderRepository]
  val sampleCommand = AddOrderItem(
    orderId,
    orderingPerson = "WHO",
    description = "Cheeseburger with chips and diet Coke",
    price = BigDecimal("15.99"))
  val handler = new AddOrderItemHandler(orderRepository)

  "Add order item handler" - {
    "returns Order not found error if order with given Id cannot be found" in {
      orderRepository.findById _ expects(orderId, *) onCall (
        (_, callback) => callback(OrderNotFound.left))

      handler.handle(
        sampleCommand,
        response => response shouldBe -\/(OrderNotFound))
    }

    "passes command to Order with given Id if it was found " +
      "and returns Order's response back" in {
      val expectedResponse = "12345".right
      orderRepository.findById _ expects(orderId, *) onCall (
        (_, callback) => callback(order.right))
      order.addItem _ expects(sampleCommand, *) onCall (
        (_, callback) => callback(expectedResponse))

      handler.handle(
        sampleCommand,
        response => response shouldBe expectedResponse)
    }
  }
}
