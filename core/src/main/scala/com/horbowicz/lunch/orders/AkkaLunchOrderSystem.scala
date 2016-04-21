package com.horbowicz.lunch.orders

import java.time.LocalDateTime
import java.util.UUID

import akka.actor.{ActorSystem, PoisonPill}
import akka.pattern.ask
import akka.persistence.query.scaladsl.{EventsByPersistenceIdQuery, ReadJournal}
import akka.stream.{ActorMaterializer, scaladsl}
import akka.util.Timeout
import com.horbowicz.lunch.orders.Global.Id
import com.horbowicz.lunch.orders.command.Command
import com.horbowicz.lunch.orders.command.error.CommandError
import com.horbowicz.lunch.orders.command.order.{AddOrderItem, OpenOrder}
import com.horbowicz.lunch.orders.common.TimeProvider
import com.horbowicz.lunch.orders.domain.IdProvider
import com.horbowicz.lunch.orders.domain.order._
import com.horbowicz.lunch.orders.query.order.GetActiveOrders
import com.horbowicz.lunch.orders.read.order.{OrdersView, OrdersViewActor}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scalaz.Scalaz._
import scalaz._

class AkkaLunchOrderSystem(
  actorSystem: ActorSystem,
  readJournal: ReadJournal with EventsByPersistenceIdQuery)
  extends LunchOrderSystem {

  private lazy val timeProvider = new TimeProvider {
    override def getCurrentDateTime: LocalDateTime = LocalDateTime.now()
  }
  private lazy val idProvider = new IdProvider {
    override def get(): Id = UUID.randomUUID().toString
  }

  implicit val mat = ActorMaterializer()(actorSystem)

  private lazy val openOrderHandler =
    actorSystem
      .actorOf(
        OpenOrderHandler.props(idProvider, timeProvider),
        "open-order-handler")
  private lazy val ordersView = actorSystem
    .actorOf(OrdersViewActor.props(new OrdersView()))

  private lazy val orders = actorSystem
    .actorOf(OrdersActor.props(idProvider, timeProvider))
  private lazy val addOrderItemHandler = actorSystem
    .actorOf(AddOrderItemHandler.props(orders))

  readJournal
    .eventsByPersistenceId("open-order-handler", 0L, Long.MaxValue)
    .map(_.event)
    .runWith(scaladsl.Sink.actorRef(ordersView, PoisonPill))
  readJournal
    .eventsByPersistenceId("open-order-handler", 0L, Long.MaxValue)
    .map(_.event)
    .runWith(scaladsl.Sink.actorRef(orders, PoisonPill))

  private implicit val timeout: Timeout = 2 second

  override def handle[Response](
    command: Command[Response]
  ): Future[CommandError \/ Response] = {
    command match {
      case openOrder: OpenOrder =>
        (openOrderHandler ? openOrder).mapTo[CommandError \/ Response]
      case addOrderItem: AddOrderItem => (addOrderItemHandler ? addOrderItem)
        .mapTo[CommandError \/ Response]
      case _ => Future.successful(new CommandError {}.left)
    }
  }

  override def handle[Response](query: GetActiveOrders.type): Future[Seq[OrdersView.Order]] = {
    (ordersView ? query).mapTo[Seq[OrdersView.Order]]
  }
}