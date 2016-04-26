package com.horbowicz.lunch.orders.command.order

case class PlaceOrder(
  orderId: String,
  personResponsible: String)
  extends OrderCommand[Unit]