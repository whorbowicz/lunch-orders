package com.horbowicz.lunch.orders.domain.order

import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.command.error.CommandError
import com.horbowicz.lunch.orders.command.order.item.AddOrderItem
import com.horbowicz.lunch.orders.common.TimeProvider
import com.horbowicz.lunch.orders.domain.order.error.InvalidOrderId
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
  override def addItem(command: AddOrderItem): CommandError \/ Id =
    if (id == command.orderId) add(command).right
    else InvalidOrderId.left

  private def add(command: AddOrderItem): Id = {
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
