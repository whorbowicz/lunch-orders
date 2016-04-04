package com.horbowicz.lunch.orders

import com.horbowicz.lunch.orders.command.Command
import com.horbowicz.lunch.orders.command.error.CommandError
import com.horbowicz.lunch.orders.query.order.GetActiveOrders
import com.horbowicz.lunch.orders.read.order.OrdersView

import scala.concurrent.Future
import scalaz._

trait LunchOrderSystem {
  def handle[Response](command: Command[Response]): Future[CommandError \/ Response]
  def handle[Response](query: GetActiveOrders.type): Future[Seq[OrdersView.Order]]
}
