package com.horbowicz.lunch.orders.event

trait EventPublisher
{
  def publish(event: Event): Unit
}
