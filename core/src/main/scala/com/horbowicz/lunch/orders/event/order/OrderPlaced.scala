package com.horbowicz.lunch.orders.event.order

import java.time.LocalDateTime

import com.horbowicz.lunch.orders.event.Event

case class OrderPlaced(
  orderId: String,
  currentDateTime: LocalDateTime,
  personResponsible: String)
  extends Event