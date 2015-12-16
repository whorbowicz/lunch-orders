package com.horbowicz.lunch.orders.domain.order

import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.domain.Order
import com.horbowicz.lunch.orders.domain.order.error.OrderNotFound

import scalaz._

trait OrderRepository
{
  type Response = OrderNotFound.type \/ Order

  def findById(id: Id, callback: Response => Unit): Unit
}
