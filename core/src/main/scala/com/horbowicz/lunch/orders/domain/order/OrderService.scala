package com.horbowicz.lunch.orders.domain.order

import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.command.order.OpenOrder
import com.horbowicz.lunch.orders.common.TimeProvider
import com.horbowicz.lunch.orders.domain.IdProvider
import com.horbowicz.lunch.orders.event.EventPublisher
import com.horbowicz.lunch.orders.event.order.OrderOpened

import scalaz.Scalaz._
import scalaz._

class OrderService(
  idProvider: IdProvider,
  timeProvider: TimeProvider,
  eventPublisher: EventPublisher)
{
  type Response = ImpossibleDeliveryTime.type \/ Id

  private def openOrder(command: OpenOrder) = {
    val id = idProvider.get()
    val createdAt = timeProvider.getCurrentDateTime
    eventPublisher
      .publish(
        OrderOpened(
          id,
          createdAt,
          command.provider,
          command.personResponsible,
          command.orderingTime,
          command.expectedDeliveryTime))
    id
  }

  def handle(
    command: OpenOrder,
    responseCallback: Response => Unit
  ): Unit =
    responseCallback(
      if (command.expectedDeliveryTime.isAfter(command.orderingTime))
        openOrder(command).right
      else
        ImpossibleDeliveryTime.left)
}
