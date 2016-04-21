package com.horbowicz.lunch.orders.domain.order

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor
import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.command.order.OpenOrder
import com.horbowicz.lunch.orders.common.TimeProvider
import com.horbowicz.lunch.orders.domain.IdProvider
import com.horbowicz.lunch.orders.domain.order.OpenOrderHandler._
import com.horbowicz.lunch.orders.domain.order.error.ImpossibleDeliveryTime
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

  override def receiveRecover: Receive = {
    case x => log.info(s"Received recover $x")
  }

  override def receiveCommand: Receive = {
    case openOrder: OpenOrder =>
      if (openOrder.expectedDeliveryTime.isAfter(openOrder.orderingTime))
        persist(createEvent(idProvider.get(), openOrder)) {
          event => sender() ! event.id.right
        }
      else sender() ! ImpossibleDeliveryTime.left
  }

  private def createEvent(id: Id, command: OpenOrder) =
    OrderOpened(
      id,
      createdAt = timeProvider.getCurrentDateTime,
      command.provider,
      command.personResponsible,
      command.orderingTime,
      command.expectedDeliveryTime)

}
