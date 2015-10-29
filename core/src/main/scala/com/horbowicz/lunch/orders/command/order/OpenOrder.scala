package com.horbowicz.lunch.orders.command.order

import java.time._

case class OpenOrder(
  provider: String,
  orderingTime: LocalTime,
  expectedDeliveryTime: LocalTime,
  personResponsible: String
)
