package com.horbowicz.lunch.orders.read.order

import java.time.{LocalDateTime, LocalTime}

import com.horbowicz.lunch.orders.BaseSpec
import com.horbowicz.lunch.orders.event.order.OrderOpened
import com.horbowicz.lunch.orders.query.order.GetActiveOrders

class OrdersViewSpec extends BaseSpec {

  private val handler = new OrdersView()
  private val foodHouseOrderOpened =
    OrderOpened(
      id = "123",
      createdAt = LocalDateTime.now(),
      provider = "Food House",
      personResponsible = "WHO",
      orderingTime = LocalTime.of(10, 15),
      expectedDeliveryTime = LocalTime.of(12, 0))
  private val leVanOrderOpened =
    OrderOpened(
      id = "234",
      createdAt = LocalDateTime.now(),
      provider = "LeVan",
      personResponsible = "HBO",
      orderingTime = LocalTime.of(11, 0),
      expectedDeliveryTime = LocalTime.of(13, 30))

  "Orders view" - {
    "returns empty sequence of active orders if no orders were added" in {
      handler.handle(GetActiveOrders) mustBe Seq.empty[OrdersView.Order]
    }

    "returns sequence of active orders that were previously added" in {
      handler.applyEvent(foodHouseOrderOpened)
      handler.applyEvent(leVanOrderOpened)

      handler.handle(GetActiveOrders) mustBe Seq(
        OrdersView.Order(
          id = foodHouseOrderOpened.id,
          state = "Opened",
          orderingPerson = foodHouseOrderOpened.personResponsible
        ),
        OrdersView.Order(
          id = leVanOrderOpened.id,
          state = "Opened",
          orderingPerson = leVanOrderOpened.personResponsible
        )
      )
    }
  }
}
