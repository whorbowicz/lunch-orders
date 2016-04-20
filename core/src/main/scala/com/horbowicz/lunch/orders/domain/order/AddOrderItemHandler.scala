package com.horbowicz.lunch.orders.domain.order

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.command.error.CommandError
import com.horbowicz.lunch.orders.command.order.{AddOrderItem, PlaceOrder}
import com.horbowicz.lunch.orders.common.callback.{CallbackHandler, _}
import com.horbowicz.lunch.orders.domain.Order
import com.horbowicz.lunch.orders.domain.order.OrdersActor.OrderFound
import com.horbowicz.lunch.orders.domain.order.error.OrderNotFound

import scala.concurrent.duration._
import scala.language.postfixOps
import scalaz.Scalaz._
import scalaz.\/

object AddOrderItemHandler {

  def props(orders: ActorRef) = Props(classOf[AddOrderItemHandler], orders)
}

class AddOrderItemHandler(orders: ActorRef)
  extends Actor with ActorLogging {

  private var callbacks = Map.empty[Id, Callback[OrderRepository#Response]]
  private var orderCallbacks = Map.empty[Id, Callback[CommandError \/ Id]]

  override def receive: Receive = {
    case OrderFound(orderId, ref) =>
      callbacks.get(orderId)
      .foreach(_.apply(wrapper(orderId, ref).right))
    case (orderId: Id, OrderNotFound) =>
      callbacks.get(orderId)
      .foreach(_.apply(OrderNotFound.left))
    case (orderId: Id, response: (CommandError \/ Id)) =>
      orderCallbacks
      .get(orderId).foreach(_.apply(response))
    case command: AddOrderItem =>
      val currentSender = sender()
      val id = command.orderId

      val callback1: Callback[\/[CommandError, Id]] = {
        response => currentSender ! response
      }
      val cb3: Callback[\/[OrderNotFound.type, Order]] =
        (response: \/[OrderNotFound.type, Order]) => {
          response.fold(
            notFound => {
              currentSender ! notFound.left
            },
            foundOrder => foundOrder.addItem(command)(callback1)
          )
        }

      orders ! OrdersActor.FindOrder(id)
      callbacks = callbacks + (id -> cb3)
  }

  private implicit val timeout: Timeout = 1 second

  private def wrapper(orderId: Id, orderRef: ActorRef): Order = new Order {
    override def addItem(
      command: AddOrderItem
    ): CallbackHandler[CommandError \/ Id] =
      (callback: Callback[CommandError \/ Id]) => {
        import context.dispatcher
        (orderRef ? command).mapTo[CommandError \/ Id]
          .map(response => (orderId, response)) pipeTo self
        orderCallbacks = orderCallbacks + (orderId -> callback)
      }

    override def place(
      command: PlaceOrder
    ): CallbackHandler[CommandError \/ Unit] = ???
  }
}
