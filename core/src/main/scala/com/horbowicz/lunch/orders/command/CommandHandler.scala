package com.horbowicz.lunch.orders.command

import com.horbowicz.lunch.orders.Global.Callback
import com.horbowicz.lunch.orders.command.error.CommandError

import scalaz.\/

trait CommandHandler[C <: Command[R], R] {

  type Response = CommandError \/ R
  type Operation = Callback[Response] => Unit

  def handle(command: C): Operation
}
