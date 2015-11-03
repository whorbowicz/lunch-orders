package com.horbowicz.lunch.orders.event

import com.horbowicz.lunch.orders.event.order.OrderOpened

trait EventPublisher
{
  def publish(event: OrderOpened): Unit
}
