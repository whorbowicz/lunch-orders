package com.horbowicz.lunch.orders.domain.order

import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.command.error.CommandError
import com.horbowicz.lunch.orders.command.order.item.AddOrderItem
import com.horbowicz.lunch.orders.common.TimeProvider
import com.horbowicz.lunch.orders.domain.{IdProvider, Order}
import com.horbowicz.lunch.orders.event.EventPublisher
import com.horbowicz.lunch.orders.event.order.item.OrderItemAdded

import scalaz.Scalaz._
import scalaz._

class OrderAggregate(
  id: Id,
  idProvider: IdProvider,
  timeProvider: TimeProvider,
  eventPublisher: EventPublisher)
  extends Order
{
  override def addItem(
    command: AddOrderItem,
    callback: (CommandError \/ Id) => Unit) =
  {
    if (id == command.orderId) callback(addItem(command).right)
    else callback(InvalidOrderId.left)
  }

  private def addItem(command: AddOrderItem): Id = {
    val id = idProvider.get()
    eventPublisher.publish(
      OrderItemAdded(
        id,
        timeProvider.getCurrentDateTime,
        command.orderId,
        command.orderingPerson,
        command.description,
        command.price))
    id
  }
}
