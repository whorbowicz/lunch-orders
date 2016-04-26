package com.horbowicz.lunch.orders.command.order

import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.command.Command

trait OrderCommand[Response] extends Command[Response] {

  def orderId: Id
}
