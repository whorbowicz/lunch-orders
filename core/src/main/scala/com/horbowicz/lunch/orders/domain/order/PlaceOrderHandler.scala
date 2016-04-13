package com.horbowicz.lunch.orders.domain.order

import com.horbowicz.lunch.orders.command.CommandHandler
import com.horbowicz.lunch.orders.command.order.PlaceOrder
import com.horbowicz.lunch.orders.common.callback._

import scalaz.Scalaz._

class PlaceOrderHandler(orderRepository: OrderRepository)
  extends CommandHandler[PlaceOrder, Unit] {

  def handle(command: PlaceOrder): Operation =
    ((callback: Response => Unit) => {
      orderRepository.findById(command.orderId) {
        (response: orderRepository.Response) => response.fold(
          notFound => notFound.left.response,
          foundOrder => foundOrder.place(command))(callback)
      }
    }).callbackHandler
}
