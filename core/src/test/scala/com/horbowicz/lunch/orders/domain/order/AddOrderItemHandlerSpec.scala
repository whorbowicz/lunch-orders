package com.horbowicz.lunch.orders.domain.order

import com.horbowicz.lunch.orders.command.order.item.AddOrderItem
import com.horbowicz.lunch.orders.domain.order.error.OrderNotFound
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
      orderRepository.findById _ expects orderId returning OrderNotFound.left

      handler.handle(sampleCommand) mustBe OrderNotFound.left
    }

    "passes command to Order with given Id if it was found " +
      "and returns Order's response back" in {
      val expectedResponse = "12345".right
      orderRepository.findById _ expects orderId returning order.right
      order.addItem _ expects sampleCommand returning expectedResponse

      handler.handle(sampleCommand) mustBe expectedResponse
    }
  }
}
