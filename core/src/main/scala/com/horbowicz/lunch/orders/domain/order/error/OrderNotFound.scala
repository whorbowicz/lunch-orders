package com.horbowicz.lunch.orders.domain.order.error

import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.command.error.CommandError

case class OrderNotFound(orderId: Id) extends CommandError
