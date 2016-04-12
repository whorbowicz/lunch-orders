package com.horbowicz.lunch.orders.common

import java.time.LocalDateTime

trait TimeProvider {

  def getCurrentDateTime: LocalDateTime

}
