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

class OrderServiceSpec extends BaseSpec
{
  val idProvider = mock[IdProvider]
  val timeProvider = mock[TimeProvider]
  val eventPublisher = mock[EventPublisher]
  val service = new OrderService(idProvider, timeProvider, eventPublisher)
  val sampleCommand = OpenOrder(
    provider = "Food House",
    personResponsible = "WHO",
    orderingTime = LocalTime.of(10, 30),
    expectedDeliveryTime = LocalTime.of(12, 30))

  "Order service" - {
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

      service.handle(sampleCommand) mustBe expectedId.right
    }

    "returns error if expected delivery time is before ordering time" in {
      val command = sampleCommand.copy(
        expectedDeliveryTime = sampleCommand.orderingTime.minusHours(1))

      service.handle(command) mustBe ImpossibleDeliveryTime.left
    }

    "returns error if expected delivery time is same as ordering time" in {
      val command = sampleCommand.copy(
        expectedDeliveryTime = sampleCommand.orderingTime)

      service.handle(command) mustBe ImpossibleDeliveryTime.left
    }
  }
}
