package com.horbowicz.lunch.orders.read.order

import java.time.LocalTime

import akka.actor.{Actor, ActorRef, Props}
import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.domain.order.error.OrderNotFound
import com.horbowicz.lunch.orders.event.Event
import com.horbowicz.lunch.orders.event.order.{OrderItemAdded, OrderOpened}
import com.horbowicz.lunch.orders.query.order.GetOrderDetails
import com.horbowicz.lunch.orders.read.order.OrdersDetails.Order.Status
import com.horbowicz.lunch.orders.read.order.OrdersDetails.{Order, OrderItem}

import scalaz.Scalaz._


object OrdersDetails {

  object Order {

    sealed trait Status

    object Status {

      case object Open extends Status

    }

    def fromEvent(event: OrderOpened) =
      Order(
        id = event.id,
        provider = event.provider,
        personResponsible = event.personResponsible,
        orderingTime = event.orderingTime,
        status = Status.Open,
        expectedDeliveryTime = event.expectedDeliveryTime,
        totalPrice = BigDecimal("0.00"),
        items = Seq()
      )
  }

  case class Order(
    id: Id,
    provider: String,
    personResponsible: String,
    orderingTime: LocalTime,
    status: Status,
    expectedDeliveryTime: LocalTime,
    totalPrice: BigDecimal,
    items: Seq[OrderItem]
  ) {

    def addItem(item: OrderItem): Order =
      copy(totalPrice = totalPrice + item.price, items = items :+ item)

  }

  object OrderItem {

    def fromEvent(event: OrderItemAdded) =
      OrderItem(
        id = event.id,
        orderingPerson = event.orderingPerson,
        description = event.description,
        price = event.price
      )
  }

  case class OrderItem(
    id: Id,
    orderingPerson: String,
    description: String,
    price: BigDecimal
  )

  def props(registerForOrderEvents: (Id, ActorRef) => Unit) = Props(
    new OrdersDetails(
      registerForOrderEvents))
}

class OrdersDetails(registerForOrderEvents: (Id, ActorRef) => Unit)
  extends Actor {

  private var orders = Map.empty[Id, Order]

  override def receive: Receive = {
    case event: OrderOpened =>
      orders = orders + (event.id -> Order.fromEvent(event))
      createDetails(event.id)
    case event: OrderItemAdded =>
      orders = orders.updated(
        event.orderId,
        orders(event.orderId).addItem(OrderItem.fromEvent(event)))
    case GetOrderDetails(orderId) => sender !
      orders.get(orderId).toRightDisjunction(OrderNotFound(orderId))
  }

  protected def createDetails(orderId: Id): ActorRef = {
    val details = context.actorOf(OrderDetails.props(orderId))
    registerForOrderEvents(orderId, details)
    details
  }
}

object OrderDetails {

  def props(orderId: Id) = Props(new OrderDetails(orderId))
}

class OrderDetails(orderId: Id) extends Actor {

  override def receive: Receive = {
    case event: Event => context.parent forward event
  }
}
