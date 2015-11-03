package com.horbowicz.lunch.orders.domain.order

import java.time._

import com.horbowicz.lunch.orders.BaseSpec
import com.horbowicz.lunch.orders.command.order.OpenOrder
import com.horbowicz.lunch.orders.domain.IdProvider

import scalaz._

class OpenOrderHandlerSpec extends BaseSpec
{
  val idProvider = mock[IdProvider]
  val handler = new OpenOrderHandler(idProvider)
  val sampleCommand = OpenOrder(
    provider = "Food House",
    personResponsible = "WHO",
    orderingTime = LocalTime.of(10, 30),
    expectedDeliveryTime = LocalTime.of(12, 30))

  "OpenOrderHandler" - {
    "returns Id of newly created order" in {
      val expectedId: String = "12345"
      idProvider.get _ expects() returning expectedId
      handler.handle(
        sampleCommand,
        response => response shouldBe \/-(expectedId))
    }

    "returns error if expected delivery time is before ordering time" in {
      val command = sampleCommand.copy(
        expectedDeliveryTime = sampleCommand.orderingTime.minusHours(1))
      handler.handle(
        command,
        response => response shouldBe -\/(ImpossibleDeliveryTime))
    }

    "returns error if expected delivery time is same as ordering time" in {
      val command = sampleCommand.copy(
        expectedDeliveryTime = sampleCommand.orderingTime)
      handler.handle(
        command,
        response => response shouldBe -\/(ImpossibleDeliveryTime))
    }
  }
}
