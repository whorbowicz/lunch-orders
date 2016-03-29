package com.horbowicz.lunch.orders
import com.horbowicz.lunch.orders.command.Command
import com.horbowicz.lunch.orders.command.error.CommandError

import scala.concurrent.{Future, Promise}
import scalaz._
import Scalaz._

class AkkaLunchOrderSystem extends LunchOrderSystem {
  override def handle[Response](command: Command[Response]): Future[CommandError \/ Response] = {
    val p = Promise[CommandError \/ Response]
    p success new CommandError{}.left
    p.future
  }
}
