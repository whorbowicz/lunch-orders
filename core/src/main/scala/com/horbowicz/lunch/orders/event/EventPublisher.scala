package com.horbowicz.lunch.orders.event

trait EventPublisher
{
  def publish[E <: Event](event: E): (E => Unit) => Unit
}
