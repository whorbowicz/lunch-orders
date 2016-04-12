package com.horbowicz.lunch.orders

object Global {

  type Id = String
  type Callback[T] = T => Unit
}
