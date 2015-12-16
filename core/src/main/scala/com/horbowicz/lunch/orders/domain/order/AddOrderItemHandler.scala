package com.horbowicz.lunch.orders.domain.order

import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.command.CommandHandler
import com.horbowicz.lunch.orders.command.order.item.AddOrderItem

import scalaz.Scalaz._

class AddOrderItemHandler(orderRepository: OrderRepository)
  extends CommandHandler[AddOrderItem, Id]
{
  def handle(
    command: AddOrderItem,
    responseCallback: Callback
  ): Unit =
    orderRepository.findById(
      command.orderId,
      findResponse => findResponse.fold(
        notFound => responseCallback(notFound.left),
        foundOrder => foundOrder.addItem(
          command,
          response => responseCallback(response))))
}
