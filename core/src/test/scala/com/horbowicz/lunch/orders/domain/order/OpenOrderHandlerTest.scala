package com.horbowicz.lunch.orders.domain.order

import java.time._

import com.horbowicz.lunch.orders.BaseSpec
import com.horbowicz.lunch.orders.command.order.OpenOrder
import com.horbowicz.lunch.orders.domain.IdProvider

import scalaz._

class OpenOrderHandlerTest extends BaseSpec
{
  val idProvider = mock[IdProvider[String]]
  val handler = new OpenOrderHandler(idProvider)
  val sampleCommand = OpenOrder(
    provider = "Food House",
    orderingTime = LocalTime.of(10, 30),
    expectedDeliveryTime = LocalTime.of(12, 30),
    personResponsible = "WHO")

  "OpenOrderHandler" - {
    "returns Id of newly created order" in {
      val expectedId: String = "12345"
      idProvider.get _ expects() returning expectedId
      val orderId = handler.handle(sampleCommand)

      orderId shouldBe \/-(expectedId)
    }

    "returns error if expected delivery time is before ordering time" in {
      val command = sampleCommand.copy(
        expectedDeliveryTime = sampleCommand.orderingTime.minusHours(1))
      val orderId = handler.handle(command)

      orderId shouldBe -\/(ImpossibleDeliveryTime)
    }

    "returns error if expected delivery time is same as ordering time" in {
      val command = sampleCommand.copy(
        expectedDeliveryTime = sampleCommand.orderingTime)
      val orderId = handler.handle(command)

      orderId shouldBe -\/(ImpossibleDeliveryTime)
    }
  }
}
