package com.horbowicz.lunch.orders.domain.order

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.horbowicz.lunch.orders.Global
import com.horbowicz.lunch.orders.common.TimeProvider
import com.horbowicz.lunch.orders.domain.IdProvider
import com.horbowicz.lunch.orders.domain.order.OrdersActor.{FindOrder, OrderFound}
import com.horbowicz.lunch.orders.domain.order.error.OrderNotFound
import com.horbowicz.lunch.orders.event.order.OrderOpened

object OrdersActor {

  def props(idProvider: IdProvider, timeProvider: TimeProvider) =
    Props(classOf[OrdersActor], idProvider, timeProvider)

  case class FindOrder(orderId: Global.Id)

  case class OrderFound(orderId: Global.Id, orderRef: ActorRef)

}

class OrdersActor(idProvider: IdProvider, timeProvider: TimeProvider)
  extends Actor with ActorLogging {

  private var orders = Map.empty[Global.Id, ActorRef]

  override def receive: Receive = {
    case event: OrderOpened =>
      log.info(s"event $event")
      orders = orders + (event.id -> createAggregate(event.id))
    case FindOrder(orderId) =>
      val searchResult = orders
        .get(orderId)
        .map(orderRef => OrderFound(orderId, orderRef))
        .getOrElse((orderId, OrderNotFound))
      sender ! searchResult
  }

  private def createAggregate(id: Global.Id): ActorRef =
    context.actorOf(OrderAggregate.props(id, idProvider, timeProvider))
}
