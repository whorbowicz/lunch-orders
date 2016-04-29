package com.horbowicz.lunch.orders

import com.horbowicz.lunch.orders.command.Command
import com.horbowicz.lunch.orders.command.error.CommandError
import com.horbowicz.lunch.orders.query.Query

import scala.concurrent.Future
import scalaz._

trait LunchOrderSystem {

  def handle[Response](command: Command[Response]): Future[CommandError \/
    Response]

  def handle[Response](query: Query[Response]): Future[Exception \/ Response]
}
