package com.horbowicz.lunch.orders.domain.order

import com.horbowicz.lunch.orders.command.error.CommandError

case object OrderNotFound extends CommandError
