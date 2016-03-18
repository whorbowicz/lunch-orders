package com.horbowicz.lunch.orders.command

import com.horbowicz.lunch.orders.command.error.CommandError

import scalaz.\/

trait CommandHandler[Command, R]
{
  type Response = CommandError \/ R

  def handle(command: Command): Response
}
