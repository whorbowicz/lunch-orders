package com.horbowicz.lunch.orders.domain.order

import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.command.order.item.AddOrderItem

import scalaz.Scalaz._
import scalaz._

class AddOrderItemHandler(orderRepository: OrderRepository)
{
  type Response = OrderNotFound.type \/ Id

  def handle(
    command: AddOrderItem,
    responseCallback: Response => Unit
  ): Unit =
    orderRepository.findById(
      command.orderId,
      findResponse => findResponse.fold(
        notFound => responseCallback(notFound.left),
        order => order.handle(
          command,
          response => responseCallback(response.right))))
}
