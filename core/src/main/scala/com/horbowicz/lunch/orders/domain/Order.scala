package com.horbowicz.lunch.orders.domain

import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.command.CommandHandler
import com.horbowicz.lunch.orders.command.error.CommandError
import com.horbowicz.lunch.orders.command.order.item.AddOrderItem

import scalaz.\/

trait Order extends CommandHandler[AddOrderItem, Id]
{
  def handle(command: AddOrderItem): Response
}
