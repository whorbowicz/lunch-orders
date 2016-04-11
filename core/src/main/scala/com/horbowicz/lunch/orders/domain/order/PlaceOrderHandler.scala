package com.horbowicz.lunch.orders.domain.order

import com.horbowicz.lunch.orders.command.CommandHandler
import com.horbowicz.lunch.orders.command.order.PlaceOrder

import scalaz.Scalaz._
import scalaz._

class PlaceOrderHandler(orderRepository: OrderRepository) extends CommandHandler[PlaceOrder, Unit]{
  def handle(command: PlaceOrder): Operation =
    callback =>
      orderRepository
        .findById(command.orderId)
        .fold(
          notFound => callback(notFound.left),
          foundOrder => foundOrder.place(command)(callback))
}
