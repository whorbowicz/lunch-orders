package com.horbowicz.lunch.orders.domain.order

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor
import com.horbowicz.lunch.orders.Global
import com.horbowicz.lunch.orders.command.order.AddOrderItem
import com.horbowicz.lunch.orders.common.TimeProvider
import com.horbowicz.lunch.orders.common.callback.CallbackHandler
import com.horbowicz.lunch.orders.domain.IdProvider
import com.horbowicz.lunch.orders.event.{Event, EventPublisher}

object OrderAggregateActor {

  def props(
    orderId: Global.Id,
    idProvider: IdProvider,
    timeProvider: TimeProvider
  ) = Props(
    classOf[OrderAggregateActor],
    orderId,
    idProvider,
    timeProvider)
}

class OrderAggregateActor(
  orderId: Global.Id,
  idProvider: IdProvider,
  timeProvider: TimeProvider)
  extends PersistentActor with ActorLogging {

  override val persistenceId: String = s"order-$orderId"

  private val order = new OrderAggregate(
    orderId, idProvider, timeProvider, new EventPublisher {
      override def publish[E <: Event](event: E): CallbackHandler[E] =
        persist(event) _
    })

  override def receiveRecover: Receive = {
    case x => log.info(s"Received recover $x")
  }

  override def receiveCommand: Receive = {
    case addOrderItem: AddOrderItem =>
      log.info(s"Received $addOrderItem")
      val currentSender = sender()
      order.addItem(addOrderItem) { response => currentSender ! response }
  }

}
