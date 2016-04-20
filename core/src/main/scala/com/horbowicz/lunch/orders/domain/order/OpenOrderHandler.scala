package com.horbowicz.lunch.orders.domain.order

import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.command.CommandHandler
import com.horbowicz.lunch.orders.command.order.OpenOrder
import com.horbowicz.lunch.orders.common.TimeProvider
import com.horbowicz.lunch.orders.common.callback._
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

  override def handle(command: OpenOrder): CallbackHandler[Response] =
    if (command.expectedDeliveryTime.isAfter(command.orderingTime))
      openOrder(command)
    else ImpossibleDeliveryTime.left.point[CallbackHandler]

  private def openOrder(command: OpenOrder): Callback[Response] => Unit =
    callback =>
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
