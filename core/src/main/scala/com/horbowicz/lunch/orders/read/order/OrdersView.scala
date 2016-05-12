package com.horbowicz.lunch.orders.read.order

import akka.actor.{Actor, ActorLogging, Props}
import com.horbowicz.lunch.orders.Global._
import com.horbowicz.lunch.orders.event.order.OrderOpened
import com.horbowicz.lunch.orders.query.order.GetActiveOrders
import com.horbowicz.lunch.orders.read.order.OrdersView._

import scalaz.\/-

object OrdersView {

  case class Order(
    id: Id,
    state: String,
    orderingPerson: String
  )

  def props = Props(classOf[OrdersView])
}

class OrdersView extends Actor with ActorLogging {

  private var orders = Vector.empty[Order]

  def receive: Receive = {
    case event: OrderOpened =>
      orders = orders :+ Order(event.id, "Open", event.personResponsible)
    case query: GetActiveOrders.type =>
      sender ! \/-(orders)
  }
}
