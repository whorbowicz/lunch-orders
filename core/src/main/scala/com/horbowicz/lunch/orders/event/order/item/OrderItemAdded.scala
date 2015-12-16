package com.horbowicz.lunch.orders.event.order.item

import java.time.LocalDateTime

import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.event.Event

case class OrderItemAdded(
  id: String,
  createdAt: LocalDateTime,
  orderId: Id,
  orderingPerson: String,
  description: String,
  price: BigDecimal)
  extends Event
