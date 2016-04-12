package com.horbowicz.lunch.orders.event

import com.horbowicz.lunch.orders.common.callback.CallbackHandler

trait EventPublisher {

  def publish[E <: Event](event: E): CallbackHandler[E]
}
