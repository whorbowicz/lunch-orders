package com.horbowicz.lunch.orders.query.order

import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.query.Query
import com.horbowicz.lunch.orders.read.order.OrdersDetails

case class GetOrderDetails(orderId: Id) extends Query[OrdersDetails.Order]
