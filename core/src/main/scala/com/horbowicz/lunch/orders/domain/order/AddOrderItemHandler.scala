package com.horbowicz.lunch.orders.domain.order

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.util.Timeout
import com.horbowicz.lunch.orders.Global
import com.horbowicz.lunch.orders.command.order.AddOrderItem
import com.horbowicz.lunch.orders.common.TimeProvider
import com.horbowicz.lunch.orders.domain.IdProvider
import com.horbowicz.lunch.orders.domain.order.OrdersActor.{FindOrder, OrderFound}
import com.horbowicz.lunch.orders.domain.order.error.OrderNotFound
import com.horbowicz.lunch.orders.event.order.OrderOpened

import scala.concurrent.duration._
import scala.language.postfixOps
import scalaz.Scalaz._

object AddOrderItemHandler {

  def props(idProvider: IdProvider, timeProvider: TimeProvider) =
    Props(
      classOf[AddOrderItemHandler],
      idProvider,
      timeProvider)
}

class AddOrderItemHandler(
  idProvider: IdProvider,
  timeProvider: TimeProvider)
  extends Actor with ActorLogging {

  private var orders = Map.empty[Global.Id, ActorRef]

  override def receive: Receive = {
    case command: AddOrderItem =>
      val id = command.orderId
      val order = orders.get(id)
      if (order.isDefined) order.foreach {
        _ forward command
      }
      else sender() ! OrderNotFound(id).left
    case FindOrder(orderId) =>
      val searchResult = orders
        .get(orderId)
        .map(orderRef => OrderFound(orderId, orderRef))
        .toRightDisjunction(OrderNotFound(orderId))
      sender ! searchResult
    case orderOpened: OrderOpened => applyEvent(orderOpened)
  }

  private def applyEvent(event: OrderOpened) =
    orders = orders + (event.id -> createAggregate(event.id))

  def createAggregate(id: Global.Id): ActorRef =
    context
      .actorOf(OrderAggregate.props(id, idProvider, timeProvider), s"order-$id")

  private implicit val timeout: Timeout = 1 second


}
