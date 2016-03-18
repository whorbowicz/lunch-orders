package com.horbowicz.lunch.orders.domain.order

import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.command.CommandHandler
import com.horbowicz.lunch.orders.command.order.OpenOrder
import com.horbowicz.lunch.orders.common.TimeProvider
import com.horbowicz.lunch.orders.domain.IdProvider
import com.horbowicz.lunch.orders.domain.order.error.ImpossibleDeliveryTime
import com.horbowicz.lunch.orders.event.EventPublisher
import com.horbowicz.lunch.orders.event.order.OrderOpened

import scalaz.Scalaz._

class OpenOrderHandler(
  idProvider: IdProvider,
  timeProvider: TimeProvider,
  eventPublisher: EventPublisher)
  extends CommandHandler[OpenOrder, Id]
{
  override def handle(command: OpenOrder): Response =
    if (command.expectedDeliveryTime.isAfter(command.orderingTime))
      openOrder(command).right
    else
      ImpossibleDeliveryTime.left

  private def openOrder(command: OpenOrder) = {
    val id = idProvider.get()
    eventPublisher.publish(createEvent(id, command))
    id
  }

  private def createEvent(id: Id, command: OpenOrder) =
    OrderOpened(
      id,
      createdAt = timeProvider.getCurrentDateTime,
      command.provider,
      command.personResponsible,
      command.orderingTime,
      command.expectedDeliveryTime)
}
