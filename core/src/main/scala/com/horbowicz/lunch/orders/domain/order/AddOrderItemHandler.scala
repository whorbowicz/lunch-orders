package com.horbowicz.lunch.orders.domain.order

import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.command.CommandHandler
import com.horbowicz.lunch.orders.command.order.AddOrderItem

import scalaz.Scalaz._

class AddOrderItemHandler(orderRepository: OrderRepository)
  extends CommandHandler[AddOrderItem, Id]
{
  override def handle(command: AddOrderItem): Operation = callback =>
    orderRepository
      .findById(command.orderId)
      .fold(
        notFound => callback(notFound.left),
        foundOrder => foundOrder.addItem(command)(callback))
}
