package com.horbowicz.lunch.orders.command.order

import com.horbowicz.lunch.orders.command.Command

case class PlaceOrder(
  orderId: String,
  personResponsible: String)
  extends Command[Unit]