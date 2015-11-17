package com.horbowicz.lunch.orders.domain.order

import com.horbowicz.lunch.orders.command.error.CommandError

case object ImpossibleDeliveryTime extends CommandError
