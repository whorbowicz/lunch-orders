package com.horbowicz.lunch.orders

import com.horbowicz.lunch.orders.command.Command
import com.horbowicz.lunch.orders.common.error.BusinessError
import com.horbowicz.lunch.orders.query.Query

import scala.concurrent.Future
import scalaz._

trait LunchOrderSystem {

  def handle[Response](command: Command[Response]): Future[BusinessError \/
    Response]

  def handle[Response](query: Query[Response]): Future[BusinessError \/
    Response]
}
