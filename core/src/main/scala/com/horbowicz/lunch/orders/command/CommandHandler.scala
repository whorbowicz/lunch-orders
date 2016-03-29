package com.horbowicz.lunch.orders.command

import com.horbowicz.lunch.orders.command.error.CommandError

import scalaz.\/

trait CommandHandler[C <: Command[R], R]
{
  type Response = CommandError \/ R

  def handle(command: C): Response
}
