package com.horbowicz.lunch.orders.domain.order

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.persistence.PersistentActor
import com.horbowicz.lunch.orders.Global
import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.command.order.{OpenOrder, OrderCommand}
import com.horbowicz.lunch.orders.common.TimeProvider
import com.horbowicz.lunch.orders.domain.IdProvider
import com.horbowicz.lunch.orders.domain.order.Orders._
import com.horbowicz.lunch.orders.domain.order.error.{ImpossibleDeliveryTime, OrderNotFound}
import com.horbowicz.lunch.orders.event.order.OrderOpened

import scalaz.Scalaz._

object Orders {

  val PersistenceId = "open-order-handler"

  def props(idProvider: IdProvider, timeProvider: TimeProvider) =
    Props(
      classOf[Orders],
      idProvider,
      timeProvider
    )
}

class Orders(
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
    case command: OrderCommand[_] =>
      val id = command.orderId
      val order = orders.get(id)
      if (order.isDefined) order.foreach {
        _ forward command
      }
      else sender() ! OrderNotFound(id).left
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

  protected def createAggregate(id: Global.Id): ActorRef =
    context
      .actorOf(OrderAggregate.props(id, idProvider, timeProvider), s"order-$id")

  override def receiveRecover: Receive = {
    case orderOpened: OrderOpened => applyEvent(orderOpened)
    case x => log.debug(s"Received recover $x")
  }
}
