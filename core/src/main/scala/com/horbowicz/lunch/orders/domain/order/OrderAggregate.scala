package com.horbowicz.lunch.orders.domain.order

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor
import com.horbowicz.lunch.orders.Global
import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.command.order.{AddOrderItem, PlaceOrder}
import com.horbowicz.lunch.orders.common.TimeProvider
import com.horbowicz.lunch.orders.domain.IdProvider
import com.horbowicz.lunch.orders.domain.order.error.{InvalidOrderId, UnfilledOrder}
import com.horbowicz.lunch.orders.event.order.{OrderItemAdded, OrderPlaced}

import scalaz.Scalaz._

object OrderAggregate {

  def props(
    orderId: Global.Id,
    idProvider: IdProvider,
    timeProvider: TimeProvider
  ) = Props(
    classOf[OrderAggregate],
    orderId,
    idProvider,
    timeProvider)

  def persistenceId(orderId: Id): String = s"order-$orderId"
}

class OrderAggregate(
  orderId: Global.Id,
  idProvider: IdProvider,
  timeProvider: TimeProvider)
  extends PersistentActor with ActorLogging {

  override val persistenceId: String = OrderAggregate.persistenceId(orderId)

  private var items = Seq.empty[Id]

  override def receiveRecover: Receive = {
    case x => log.debug(s"Received recover $x")
  }

  override def receiveCommand: Receive = {
    case addOrderItem: AddOrderItem =>
      log.debug(s"Received $addOrderItem")
      val currentSender = sender()
      if (orderId != addOrderItem.orderId) currentSender ! InvalidOrderId.left
      else persist(orderItemAdded(idProvider.get(), addOrderItem)) {
        event =>
          applyEvent(event)
          currentSender ! event.id.right
      }
    case placeOrder: PlaceOrder =>
      log.debug(s"Received $placeOrder")
      val currentSender = sender()
      if (orderId != placeOrder.orderId) currentSender ! InvalidOrderId.left
      else if (items.isEmpty) currentSender ! UnfilledOrder.left
      else persist(orderPlaced(placeOrder)) {
        _ => currentSender ! ().right
      }
  }

  private def orderItemAdded(itemId: Id, command: AddOrderItem) =
    OrderItemAdded(
      itemId,
      timeProvider.getCurrentDateTime,
      command.orderId,
      command.orderingPerson,
      command.description,
      command.price)

  private def orderPlaced(command: PlaceOrder): OrderPlaced =
    OrderPlaced(
      orderId,
      timeProvider.getCurrentDateTime,
      command.personResponsible)

  private def applyEvent(event: OrderItemAdded) = event match {
    case OrderItemAdded(itemId, _, oId, _, _, _) if oId == this.orderId =>
      items = items :+ itemId
  }
}

