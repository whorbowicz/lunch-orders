package com.horbowicz.lunch.orders.domain.order

import com.horbowicz.lunch.orders.command.order.AddOrderItem
import com.horbowicz.lunch.orders.common.callback._
import com.horbowicz.lunch.orders.domain.order.error.OrderNotFound
import com.horbowicz.lunch.orders.{BaseSpec, domain}

import scalaz.Scalaz._

class AddOrderItemHandlerSpec extends BaseSpec {

  private val orderId = "123"
  private val order = mock[domain.Order]
  private val orderRepository = mock[OrderRepository]
  private val handler = new AddOrderItemHandler(orderRepository)
  private val sampleCommand = AddOrderItem(
    orderId,
    orderingPerson = "WHO",
    description = "Cheeseburger with chips and diet Coke",
    price = BigDecimal("15.99"))

  "Add order item handler" - {
    "returns Order not found error if order with given Id cannot be found" in {
      orderRepository.findById _ expects orderId returning
        OrderNotFound.left.point[CallbackHandler]

      handler.handle(sampleCommand) {
        response => response mustBe OrderNotFound.left
      }
    }

    "passes command to Order with given Id if it was found " +
      "and returns Order's response back" in {
      val expectedId = "12345"
      orderRepository.findById _ expects orderId returning
        order.right.point[CallbackHandler]
      order.addItem _ expects sampleCommand returning
        expectedId.right.point[CallbackHandler]

      handler.handle(sampleCommand) {
        response => response mustBe expectedId.right
      }
    }
  }
}
