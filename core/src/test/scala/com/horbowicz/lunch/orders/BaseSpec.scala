package com.horbowicz.lunch.orders

import org.scalamock.scalatest.MockFactory
import org.scalatest._

trait BaseSpec
  extends FreeSpecLike with MustMatchers with MockFactory with BeforeAndAfter
