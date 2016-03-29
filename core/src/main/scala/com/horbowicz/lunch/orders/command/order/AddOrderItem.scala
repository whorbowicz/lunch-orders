package com.horbowicz.lunch.orders.command.order

import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.command.Command

case class AddOrderItem(
  orderId: Id,
  orderingPerson: String,
  description: String,
  price: BigDecimal)
extends Command[Id]
