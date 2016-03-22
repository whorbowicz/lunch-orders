package com.horbowicz.lunch.orders.domain.order

import java.time._

import com.horbowicz.lunch.orders.BaseSpec
import com.horbowicz.lunch.orders.command.order.OpenOrder
import com.horbowicz.lunch.orders.common.TimeProvider
import com.horbowicz.lunch.orders.domain.IdProvider
import com.horbowicz.lunch.orders.domain.order.error.ImpossibleDeliveryTime
import com.horbowicz.lunch.orders.event.EventPublisher
import com.horbowicz.lunch.orders.event.order.OrderOpened

import scalaz._
import Scalaz._

class OpenOrderHandlerSpec extends BaseSpec
{
  private val idProvider = mock[IdProvider]
  private val timeProvider = mock[TimeProvider]
  private val eventPublisher = mock[EventPublisher]
  private val handler = new OpenOrderHandler(idProvider, timeProvider, eventPublisher)
  private val sampleCommand = OpenOrder(
    provider = "Food House",
    personResponsible = "WHO",
    orderingTime = LocalTime.of(10, 30),
    expectedDeliveryTime = LocalTime.of(12, 30))

  "Open order handler" - {
    "publishes OrderOpened event and returns Id of newly opened order" in {
      val expectedId: String = "12345"
      val currentDateTime = LocalDateTime.now()
      idProvider.get _ expects() returning expectedId
      timeProvider.getCurrentDateTime _ expects() returning currentDateTime
      eventPublisher.publish _ expects OrderOpened(
        expectedId,
        currentDateTime,
        sampleCommand.provider,
        sampleCommand.personResponsible,
        sampleCommand.orderingTime,
        sampleCommand.expectedDeliveryTime)

      handler.handle(sampleCommand) mustBe expectedId.right
    }

    "returns error if expected delivery time is before ordering time" in {
      val command = sampleCommand.copy(
        expectedDeliveryTime = sampleCommand.orderingTime.minusHours(1))

      handler.handle(command) mustBe ImpossibleDeliveryTime.left
    }

    "returns error if expected delivery time is same as ordering time" in {
      val command = sampleCommand.copy(
        expectedDeliveryTime = sampleCommand.orderingTime)

      handler.handle(command) mustBe ImpossibleDeliveryTime.left
    }
  }
}
