package com.horbowicz.lunch.orders.domain.order

import com.horbowicz.lunch.orders.command.CommandHandler
import com.horbowicz.lunch.orders.command.order.PlaceOrder
import scalaz._
import Scalaz._

class PlaceOrderHandler(orderRepository: OrderRepository) extends CommandHandler[PlaceOrder, Unit]{
  def handle(command: PlaceOrder): Response =
    orderRepository
      .findById(command.orderId)
      .fold(
        notFound => notFound.left,
        foundOrder => foundOrder.place(command))
}
