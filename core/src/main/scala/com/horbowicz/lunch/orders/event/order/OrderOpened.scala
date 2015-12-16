package com.horbowicz.lunch.orders.event.order

import java.time.{LocalDateTime, LocalTime}

import com.horbowicz.lunch.orders.Global._
import com.horbowicz.lunch.orders.event.Event

case class OrderOpened(
  id: Id,
  createdAt: LocalDateTime,
  provider: String,
  personResponsible: String,
  orderingTime: LocalTime,
  expectedDeliveryTime: LocalTime)
  extends Event
