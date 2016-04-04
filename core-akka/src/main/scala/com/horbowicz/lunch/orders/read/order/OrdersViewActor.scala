package com.horbowicz.lunch.orders.read.order

import akka.actor.{Actor, ActorLogging, Props}
import akka.persistence.query.PersistenceQuery
import com.horbowicz.lunch.orders.event.order.OrderOpened
import com.horbowicz.lunch.orders.query.order.GetActiveOrders

object OrdersViewActor {
  def props(handler: OrdersView) = Props(classOf[OrdersViewActor], handler)
}

class OrdersViewActor(handler: OrdersView) extends Actor with ActorLogging {

  def receive: Receive = {
    case event: OrderOpened =>
      log.info(s"event $event")
      handler.applyEvent(event)
    case query: GetActiveOrders.type =>
      log.debug(s"query $query")
      sender ! handler.handle(query)
  }
}
