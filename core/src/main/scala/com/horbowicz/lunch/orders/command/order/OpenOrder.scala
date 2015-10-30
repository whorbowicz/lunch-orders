package com.horbowicz.lunch.orders.command.order

import java.time._

case class OpenOrder(
  provider: String,
  personResponsible: String,
  orderingTime: LocalTime,
  expectedDeliveryTime: LocalTime)
