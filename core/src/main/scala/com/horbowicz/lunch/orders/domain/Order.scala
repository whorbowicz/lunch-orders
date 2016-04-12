package com.horbowicz.lunch.orders.domain

import com.horbowicz.lunch.orders.Global._
import com.horbowicz.lunch.orders.command.error.CommandError
import com.horbowicz.lunch.orders.command.order.{AddOrderItem, PlaceOrder}
import com.horbowicz.lunch.orders.common.callback.CallbackHandler

import scalaz.\/

trait Order {

  def addItem(command: AddOrderItem): CallbackHandler[CommandError \/ Id]

  def place(command: PlaceOrder): CallbackHandler[CommandError \/ Unit]
}
