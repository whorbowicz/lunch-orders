package com.horbowicz.lunch.orders.domain.order

import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.command.CommandHandler
import com.horbowicz.lunch.orders.command.order.AddOrderItem
import com.horbowicz.lunch.orders.common.callback._

import scalaz.Scalaz._

class AddOrderItemHandler(orderRepository: OrderRepository)
  extends CommandHandler[AddOrderItem, Id] {

  override def handle(command: AddOrderItem): Operation = {
    ((callback: Response => Unit) => {
      orderRepository.findById(command.orderId) {
        (response: orderRepository.Response) => response.fold(
          notFound => notFound.left.response,
          foundOrder => foundOrder.addItem(command))(callback)
      }
    }).callbackHandler
  }
}
