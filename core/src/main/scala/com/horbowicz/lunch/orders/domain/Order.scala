package com.horbowicz.lunch.orders.domain

import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.command.order.item.AddOrderItem

trait Order
{
  def handle(command: AddOrderItem, callback: Id => Unit)

}