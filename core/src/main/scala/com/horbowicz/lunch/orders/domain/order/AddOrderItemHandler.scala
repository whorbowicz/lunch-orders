package com.horbowicz.lunch.orders.domain.order

import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.command.CommandHandler
import com.horbowicz.lunch.orders.command.order.item.AddOrderItem

import scalaz.Scalaz._

class AddOrderItemHandler(orderRepository: OrderRepository)
  extends CommandHandler[AddOrderItem, Id]
{
  override def handle(command: AddOrderItem): Response =
    orderRepository
      .findById(command.orderId)
      .fold(
        notFound => notFound.left,
        foundOrder => foundOrder.handle(command))
}
