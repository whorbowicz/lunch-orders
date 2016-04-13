package com.horbowicz.lunch.orders.domain.order

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor
import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.command.CommandHandler
import com.horbowicz.lunch.orders.command.order.OpenOrder
import com.horbowicz.lunch.orders.common.callback._
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
      override def publish[E <: Event](event: E): CallbackHandler[E] =
        (persist(event) _).callbackHandler
    })

  override def persistenceId: String = "open-order-handler"

  override def receiveRecover: Receive = {
    case x => log.info(s"Received recover $x")
  }

  override def receiveCommand: Receive = {
    case openOrder: OpenOrder =>
      log.info(s"Received $openOrder")
      val currentSender = sender()
      handler.handle(openOrder)(response => currentSender ! response)
  }

}
