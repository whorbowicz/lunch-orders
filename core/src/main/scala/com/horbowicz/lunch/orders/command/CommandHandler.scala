package com.horbowicz.lunch.orders.command

import com.horbowicz.lunch.orders.command.error.CommandError
import com.horbowicz.lunch.orders.common.callback.CallbackHandler

import scalaz.\/

trait CommandHandler[C <: Command[R], R] {

  type Response = CommandError \/ R
  type Operation = CallbackHandler[Response]

  def handle(command: C): Operation
}
