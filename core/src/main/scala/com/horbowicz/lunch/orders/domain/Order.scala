package com.horbowicz.lunch.orders.domain

import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.command.error.CommandError
import com.horbowicz.lunch.orders.command.order.item.AddOrderItem

import scalaz.\/

trait Order
{
  def addItem(
    command: AddOrderItem,
    callback: (CommandError \/ Id) => Unit): Unit
}
