package com.horbowicz.lunch.orders

import com.horbowicz.lunch.orders.command.Command
import com.horbowicz.lunch.orders.command.error.CommandError

import scala.concurrent.Future
import scalaz._

trait LunchOrderSystem {
  def handle[Response](command: Command[Response]): Future[CommandError \/ Response]
}
