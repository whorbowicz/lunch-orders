package com.horbowicz.lunch.orders.domain.order

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.persistence.PersistentActor
import com.horbowicz.lunch.orders.Global
import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.command.order.OpenOrder
import com.horbowicz.lunch.orders.common.TimeProvider
import com.horbowicz.lunch.orders.domain.IdProvider
import com.horbowicz.lunch.orders.domain.order.OpenOrderHandler._
import com.horbowicz.lunch.orders.domain.order.OrdersActor.{FindOrder, OrderFound}
import com.horbowicz.lunch.orders.domain.order.error.{ImpossibleDeliveryTime, OrderNotFound}
import com.horbowicz.lunch.orders.event.order.OrderOpened

import scalaz.Scalaz._

object OpenOrderHandler {

  val PersistenceId = "open-order-handler"

  def props(idProvider: IdProvider, timeProvider: TimeProvider) =
    Props(
      classOf[OpenOrderHandler],
      idProvider,
      timeProvider
    )
}

class OpenOrderHandler(
  idProvider: IdProvider,
  timeProvider: TimeProvider)
  extends PersistentActor with ActorLogging {

  override def persistenceId: String = PersistenceId

  private var orders = Map.empty[Global.Id, ActorRef]

  override def receiveCommand: Receive = {
    case openOrder: OpenOrder =>
      if (openOrder.expectedDeliveryTime.isAfter(openOrder.orderingTime))
        persist(createEvent(idProvider.get(), openOrder)) {
          event =>
            applyEvent(event)
            sender() ! event.id.right
        }
      else sender() ! ImpossibleDeliveryTime.left
    case FindOrder(orderId) =>
      val searchResult = orders
        .get(orderId)
        .map(orderRef => OrderFound(orderId, orderRef))
        .toRightDisjunction(OrderNotFound(orderId))
      sender ! searchResult
  }

  private def createEvent(id: Id, command: OpenOrder) =
    OrderOpened(
      id,
      createdAt = timeProvider.getCurrentDateTime,
      command.provider,
      command.personResponsible,
      command.orderingTime,
      command.expectedDeliveryTime)

  private def applyEvent(event: OrderOpened) =
    orders = orders + (event.id -> createAggregate(event.id))

  private def createAggregate(id: Global.Id): ActorRef =
    context
      .actorOf(OrderAggregate.props(id, idProvider, timeProvider), s"order-$id")

  override def receiveRecover: Receive = {
    case orderOpened: OrderOpened => applyEvent(orderOpened)
    case x => log.info(s"Received recover $x")
  }
}
