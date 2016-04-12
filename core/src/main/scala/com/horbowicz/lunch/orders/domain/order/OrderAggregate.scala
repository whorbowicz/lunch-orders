package com.horbowicz.lunch.orders.domain.order

import com.horbowicz.lunch.orders.Global.{Callback, Id}
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
  extends Order {

  private var items = Seq.empty[Id]

  override def addItem(
    command: AddOrderItem
  ): Callback[CommandError \/ Id] => Unit =
    callback =>
      if (id != command.orderId) callback(InvalidOrderId.left)
      else add(command, callback)

  private def add(
    command: AddOrderItem,
    callback: Callback[CommandError \/ Id]) =
    eventPublisher.publish(createEvent(idProvider.get(), command)) {
      event => callback(event.id.right)
    }

  private def createEvent(id: Id, command: AddOrderItem) =
    OrderItemAdded(
      id,
      timeProvider.getCurrentDateTime,
      command.orderId,
      command.orderingPerson,
      command.description,
      command.price)

  override def place(
    command: PlaceOrder
  ): Callback[CommandError \/ Unit] => Unit =
    callback =>
      if (id != command.orderId) callback(InvalidOrderId.left)
      else if (items.isEmpty) callback(UnfilledOrder.left)
      else placeOrder(command, callback)

  private def placeOrder(
    command: PlaceOrder,
    callback: Callback[CommandError \/ Unit]
  ): Unit =
    eventPublisher.publish(createEvent(command)) {
      _ => callback(().right)
    }

  private def createEvent(command: PlaceOrder): OrderPlaced =
    OrderPlaced(id, timeProvider.getCurrentDateTime, command.personResponsible)

  def applyEvent(event: OrderItemAdded) = event match {
    case OrderItemAdded(itemId, _, this.id, _, _, _) => items = items :+ itemId
  }
}
