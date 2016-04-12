package com.horbowicz.lunch.orders.domain

import com.horbowicz.lunch.orders.Global.{Callback, Id}
import com.horbowicz.lunch.orders.command.error.CommandError
import com.horbowicz.lunch.orders.command.order.{AddOrderItem, PlaceOrder}

import scalaz.\/

trait Order {

  def addItem(command: AddOrderItem): Callback[CommandError \/ Id] => Unit

  def place(command: PlaceOrder): Callback[CommandError \/ Unit] => Unit
}
