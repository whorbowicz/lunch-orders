package com.horbowicz.lunch.orders.read.order

import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.event.order.OrderOpened
import com.horbowicz.lunch.orders.query.order.GetActiveOrders
import com.horbowicz.lunch.orders.read.order.OrdersView.Order


object OrdersView {

  case class Order(
    id: Id,
    state: String,
    orderingPerson: String
  )

}

class OrdersView {

  private var orders = Seq.empty[OrdersView.Order]

  def applyEvent(event: OrderOpened): Unit = {
    orders = orders :+ Order(event.id, "Open", event.personResponsible)
  }

  def handle(query: GetActiveOrders.type): Seq[OrdersView.Order] = orders
}
