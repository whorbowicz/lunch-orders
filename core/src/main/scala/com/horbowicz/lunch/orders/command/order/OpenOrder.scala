package com.horbowicz.lunch.orders.command.order

import java.time._

import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.command.Command

case class OpenOrder(
  provider: String,
  personResponsible: String,
  orderingTime: LocalTime,
  expectedDeliveryTime: LocalTime)
  extends Command[Id]
