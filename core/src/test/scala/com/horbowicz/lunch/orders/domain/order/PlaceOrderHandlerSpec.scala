package com.horbowicz.lunch.orders.domain.order

import com.horbowicz.lunch.orders.command.order.PlaceOrder
import com.horbowicz.lunch.orders.common.callback._
import com.horbowicz.lunch.orders.domain.order.error.OrderNotFound
import com.horbowicz.lunch.orders.{BaseSpec, domain}

import scalaz.Scalaz._

class PlaceOrderHandlerSpec extends BaseSpec {

  private val orderId = "123"
  private val order = mock[domain.Order]
  private val orderRepository = mock[OrderRepository]
  private val handler = new PlaceOrderHandler(orderRepository)
  private val sampleCommand = PlaceOrder(
    orderId,
    personResponsible = "WHO")

  "Place order handler" - {
    "returns Order not found error if order with given Id cannot be found" in {
      orderRepository.findById _ expects orderId returning
        OrderNotFound.left.response

      handler.handle(sampleCommand) {
        response => response mustBe OrderNotFound.left
      }
    }

    "passes command to Order with given Id if it was found " +
      "and returns Order's response back" in {
      orderRepository.findById _ expects orderId returning order.right.response
      order.place _ expects sampleCommand returning
        ().right.response

      handler.handle(sampleCommand) {
        response => response mustBe ().right
      }
    }
  }
}
