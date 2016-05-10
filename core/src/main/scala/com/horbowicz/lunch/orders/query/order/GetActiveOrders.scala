package com.horbowicz.lunch.orders.query.order

import com.horbowicz.lunch.orders.query.Query
import com.horbowicz.lunch.orders.read.order.OrdersView

case object GetActiveOrders extends Query[Seq[OrdersView.Order]]
