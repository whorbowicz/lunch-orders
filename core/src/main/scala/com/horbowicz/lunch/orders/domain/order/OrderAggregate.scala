package com.horbowicz.lunch.orders.domain.order

import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.command.error.CommandError
import com.horbowicz.lunch.orders.command.order.{AddOrderItem, PlaceOrder}
import com.horbowicz.lunch.orders.common.TimeProvider
import com.horbowicz.lunch.orders.domain.order.error.{InvalidOrderId, UnfilledOrder}
import com.horbowicz.lunch.orders.domain.{IdProvider, Order}
import com.horbowicz.lunch.orders.event.EventPublisher
import com.horbowicz.lunch.orders.event.order.{OrderItemAdded, OrderPlaced}

import scalaz.Scalaz._
import scalaz._

class OrderAggregate(
  id: Id,
  idProvider: IdProvider,
  timeProvider: TimeProvider,
  eventPublisher: EventPublisher)
  extends Order
{
  private var items = Seq.empty[Id]

  override def addItem(command: AddOrderItem): CommandError \/ Id =
    if (id != command.orderId) InvalidOrderId.left
    else add(command).right

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

  override def place(command: PlaceOrder): CommandError \/ Unit =
    if(id != command.orderId) InvalidOrderId.left
    else if(items.isEmpty) UnfilledOrder.left
    else {
      eventPublisher.publish(OrderPlaced(id, timeProvider.getCurrentDateTime, command.personResponsible))
      ().right
    }

  def applyEvent(event: OrderItemAdded) = event match {
    case OrderItemAdded(itemId, _, this.id, _, _, _) => items = items :+ itemId
  }
}
