package com.horbowicz.lunch.orders.domain.order

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.TestProbe
import com.horbowicz.lunch.orders._
import com.horbowicz.lunch.orders.command.order.AddOrderItem
import com.horbowicz.lunch.orders.domain.order.OrdersActor.{FindOrder, OrderFound}
import com.horbowicz.lunch.orders.domain.order.error.OrderNotFound

import scala.concurrent.duration._
import scala.language.postfixOps
import scalaz.Scalaz._

class AddOrderItemHandlerSpec
  extends BaseActorSpec(ActorSystem("AddOrderItemHandlerSpec")) {

  private val orderId = "123"
  private val addOrderItem = AddOrderItem(
    orderId,
    orderingPerson = "WHO",
    description = "Cheeseburger with chips and diet Coke",
    price = BigDecimal("15.99"))
  private var handler: ActorRef = _
  private val ordersProbe = TestProbe()
  private val orderProbe = TestProbe()


  before {
    handler = system.actorOf(AddOrderItemHandler.props(ordersProbe.ref))
  }

  "Add order item handler" - {
    "returns Order not found error if order with given Id cannot be found" in {
      within(1 second) {
        handler ! addOrderItem
        ordersProbe.expectMsg(FindOrder(orderId))
        ordersProbe.reply((orderId, OrderNotFound))
        expectMsg(OrderNotFound.left)
      }
    }

    "passes command to Order with given Id if it was found " +
      "and returns Order's response back" in {
      val expectedId = "12345"

      within(1 second) {
        handler ! addOrderItem
        ordersProbe.expectMsg(FindOrder(orderId))
        ordersProbe.reply(OrderFound(orderId, orderProbe.ref))
        orderProbe.expectMsg(addOrderItem)
        orderProbe.reply(expectedId.right)
        expectMsg(expectedId.right)
      }
    }
  }
}
