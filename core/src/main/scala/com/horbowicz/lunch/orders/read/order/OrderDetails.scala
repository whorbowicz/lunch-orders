package com.horbowicz.lunch.orders.read.order

import java.time.LocalTime

import akka.actor.{Actor, Props}
import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.read.order.OrderDetails.Order.Status

object OrderDetails {

  object Order {

    sealed trait Status

    object Status {

      case object Open extends Status

    }

  }

  case class Order(
    id: Id,
    provider: String,
    personResponsible: String,
    orderingTime: LocalTime,
    status: Status,
    expectedDeliveryTime: LocalTime,
    totalPrice: BigDecimal,
    items: Seq[OrderItem]
  )


  case class OrderItem(
    orderingPerson: String,
    description: String,
    price: BigDecimal
  )

  def props = Props(classOf[OrderDetails])
}

class OrderDetails extends Actor {

  override def receive: Receive = ???
}
