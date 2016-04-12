package com.horbowicz.lunch.orders.event

import com.horbowicz.lunch.orders.Global.Callback

trait EventPublisher {

  def publish[E <: Event](event: E): Callback[E] => Unit
}
