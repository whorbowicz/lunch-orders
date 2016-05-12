package com.horbowicz.lunch.orders.domain.order.error

import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.common.error.BusinessError

case class OrderNotFound(orderId: Id) extends BusinessError
