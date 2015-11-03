package com.horbowicz.lunch.orders.domain.order

import com.horbowicz.lunch.orders.Global._
import com.horbowicz.lunch.orders.command.order.OpenOrder
import com.horbowicz.lunch.orders.domain.IdProvider

import scalaz._

class OpenOrderHandler(idProvider: IdProvider)
{
  type Response = ImpossibleDeliveryTime.type \/ Id

  def handle(
    command: OpenOrder,
    responseCallback: Response => Unit
  ): Unit =
    responseCallback(
      if (command.expectedDeliveryTime.isAfter(command.orderingTime))
        \/-(idProvider.get())
      else
        -\/(ImpossibleDeliveryTime))
}
