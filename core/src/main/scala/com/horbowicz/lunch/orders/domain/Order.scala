package com.horbowicz.lunch.orders.domain

import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.command.error.CommandError
import com.horbowicz.lunch.orders.command.order.{AddOrderItem, PlaceOrder}

import scalaz.\/

trait Order
{
  def addItem(command: AddOrderItem): (CommandError \/ Id => Unit) => Unit
  def place(command: PlaceOrder): (CommandError \/ Unit => Unit) => Unit
}
