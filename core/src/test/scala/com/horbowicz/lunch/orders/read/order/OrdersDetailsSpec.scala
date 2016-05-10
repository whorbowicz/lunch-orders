package com.horbowicz.lunch.orders.read.order

import java.time._

import akka.actor.{ActorRef, ActorSystem}
import com.horbowicz.lunch.orders.BaseActorSpec
import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.domain.order.error.OrderNotFound
import com.horbowicz.lunch.orders.event.order.{OrderItemAdded, OrderOpened}
import com.horbowicz.lunch.orders.query.order.GetOrderDetails

import scalaz.Scalaz._

class OrdersDetailsSpec extends BaseActorSpec(ActorSystem("OrderDetailsSpec")) {

  private var ordersDetails: ActorRef = _
  private val orderId: Id = "123"
  private val foodHouseOrderOpened =
    OrderOpened(
      id = orderId,
      createdAt = LocalDateTime.now(),
      provider = "Food House",
      personResponsible = "WHO",
      orderingTime = LocalTime.of(10, 15),
      expectedDeliveryTime = LocalTime.of(12, 0))
  private val hboOrder =
    OrderItemAdded(
      "234",
      LocalDate.now().atTime(10, 0),
      orderId,
      "HBO",
      "Meat dumplings, salad",
      BigDecimal("15.50"))
  private val pprOrder =
    OrderItemAdded(
      "234",
      LocalDate.now().atTime(10, 0),
      orderId,
      "PPR",
      "Spahetti bolognese",
      BigDecimal("12.00"))

  private val registerForOrderEvents: (Id, ActorRef) => Unit =
    (orderId, actor) => ()

  before {
    ordersDetails = system.actorOf(OrdersDetails.props(registerForOrderEvents))
  }

  "Orders details" - {
    "returns Order not found error if requested order was not opened" in {
      ordersDetails ! GetOrderDetails(orderId)
      expectMsg(OrderNotFound(orderId).left)
    }

    "returns order details if order was opened" in {
      ordersDetails ! foodHouseOrderOpened
      ordersDetails ! GetOrderDetails(orderId)
      expectMsg(
        OrdersDetails.Order(
          id = orderId,
          provider = foodHouseOrderOpened.provider,
          personResponsible = foodHouseOrderOpened.personResponsible,
          orderingTime = foodHouseOrderOpened.orderingTime,
          status = OrdersDetails.Order.Status.Open,
          expectedDeliveryTime = foodHouseOrderOpened.expectedDeliveryTime,
          totalPrice = BigDecimal("0.00"),
          items = Seq()
        ).right)
    }

    "returns order details, items list and total price if " +
      "order was opened and items were added to it" in {
      ordersDetails ! foodHouseOrderOpened
      ordersDetails ! hboOrder
      ordersDetails ! pprOrder
      ordersDetails ! GetOrderDetails(orderId)
      expectMsg(
        OrdersDetails.Order(
          id = orderId,
          provider = foodHouseOrderOpened.provider,
          personResponsible = foodHouseOrderOpened.personResponsible,
          orderingTime = foodHouseOrderOpened.orderingTime,
          status = OrdersDetails.Order.Status.Open,
          expectedDeliveryTime = foodHouseOrderOpened.expectedDeliveryTime,
          totalPrice = BigDecimal("27.50"),
          items = Seq(
            OrdersDetails.OrderItem(
              hboOrder.id,
              hboOrder.orderingPerson,
              hboOrder.description,
              hboOrder.price
            ),
            OrdersDetails.OrderItem(
              pprOrder.id,
              pprOrder.orderingPerson,
              pprOrder.description,
              pprOrder.price
            )
          )
        ).right)
    }
  }
}
