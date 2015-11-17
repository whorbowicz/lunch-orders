package com.horbowicz.lunch.orders.command

import com.horbowicz.lunch.orders.command.error.CommandError

import scalaz.\/

trait CommandHandler[Command, R]
{
  type Response = CommandError \/ R
  type Callback = Response => Unit

  def handle(command: Command, callback: Callback)
}
