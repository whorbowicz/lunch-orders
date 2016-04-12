package com.horbowicz.lunch.orders.domain

import com.horbowicz.lunch.orders.Global._

trait IdProvider {

  def get(): Id
}
