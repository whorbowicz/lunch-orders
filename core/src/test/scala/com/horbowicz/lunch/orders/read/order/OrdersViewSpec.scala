package com.horbowicz.lunch.orders.read.order

import java.time.{LocalDateTime, LocalTime}

import akka.actor.{ActorRef, ActorSystem}
import com.horbowicz.lunch.orders.BaseActorSpec
import com.horbowicz.lunch.orders.event.order.OrderOpened
import com.horbowicz.lunch.orders.query.order.GetActiveOrders

import scalaz.Scalaz._


class OrdersViewSpec extends BaseActorSpec(ActorSystem("OrdersViewSpec")) {

  private var ordersView: ActorRef = _
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


  before {
    ordersView = system.actorOf(OrdersView.props)
  }

  "Orders view" - {
    "returns empty sequence of active orders if no orders were added" in {
      ordersView ! GetActiveOrders
      expectMsg(Seq.empty[OrdersView.Order].right)
    }

    "returns sequence of active orders that were previously added" in {
      ordersView ! foodHouseOrderOpened
      ordersView ! leVanOrderOpened

      ordersView ! GetActiveOrders
      expectMsg(
        Seq(
          OrdersView.Order(
            id = foodHouseOrderOpened.id,
            state = "Open",
            orderingPerson = foodHouseOrderOpened.personResponsible
          ),
          OrdersView.Order(
            id = leVanOrderOpened.id,
            state = "Open",
            orderingPerson = leVanOrderOpened.personResponsible
          )
        ).right
      )
    }
  }
}
