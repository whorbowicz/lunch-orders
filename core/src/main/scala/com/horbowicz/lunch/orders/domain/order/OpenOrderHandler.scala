package com.horbowicz.lunch.orders.domain.order

import com.horbowicz.lunch.orders.Global.{Callback, Id}
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
  extends CommandHandler[OpenOrder, Id] {

  override def handle(command: OpenOrder): Operation =
    callback =>
      if (command.expectedDeliveryTime.isAfter(command.orderingTime))
        openOrder(command)(callback)
      else callback(ImpossibleDeliveryTime.left)

  private def openOrder(command: OpenOrder)(callback: Callback[Response]) =
    eventPublisher.publish(createEvent(idProvider.get(), command)) {
      event => callback(event.id.right)
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
