package com.horbowicz.lunch.orders.domain.order

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor
import com.horbowicz.lunch.orders.Global.{Callback, Id}
import com.horbowicz.lunch.orders.command.CommandHandler
import com.horbowicz.lunch.orders.command.order.OpenOrder
import com.horbowicz.lunch.orders.event.{Event, EventPublisher}

object OpenOrderHandlerActor {

  def props(handlerFactory: EventPublisher => CommandHandler[OpenOrder, Id]) =
    Props(
      classOf[OpenOrderHandlerActor],
      handlerFactory)
}

class OpenOrderHandlerActor(
  handlerFactory: EventPublisher => CommandHandler[OpenOrder, Id])
  extends PersistentActor with ActorLogging {

  private val handler: CommandHandler[OpenOrder, Id] = handlerFactory(
    new EventPublisher {
      override def publish[E <: Event](event: E): Callback[E] => Unit =
        callback => persist(event)(callback)
    })

  override def persistenceId: String = "open-order-handler"

  override def receiveRecover: Receive = {
    case _ =>
  }

  override def receiveCommand: Receive = {
    case openOrder: OpenOrder =>
      log.debug(s"Received $openOrder")
      handler.handle(openOrder)(response => sender() ! response)
  }

}
