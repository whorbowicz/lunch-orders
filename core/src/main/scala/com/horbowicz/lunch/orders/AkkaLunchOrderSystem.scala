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
import com.horbowicz.lunch.orders.command.order._
import com.horbowicz.lunch.orders.common.TimeProvider
import com.horbowicz.lunch.orders.domain.IdProvider
import com.horbowicz.lunch.orders.domain.order._
import com.horbowicz.lunch.orders.query.Query
import com.horbowicz.lunch.orders.query.order.{GetActiveOrders, GetOrderDetails}
import com.horbowicz.lunch.orders.read.order._

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

  private lazy val orders =
    actorSystem
      .actorOf(
        Orders.props(idProvider, timeProvider),
        "open-order-handler")
  private lazy val ordersView = actorSystem.actorOf(OrdersView.props)
  private lazy val orderDetails = actorSystem.actorOf(OrderDetails.props)

  readJournal
    .eventsByPersistenceId("open-order-handler", 0L, Long.MaxValue)
    .map(_.event)
    .runWith(scaladsl.Sink.actorRef(ordersView, PoisonPill))

  private implicit val timeout: Timeout = 2 second

  override def handle[Response](
    command: Command[Response]
  ): Future[CommandError \/ Response] = {
    command match {
      case openOrder: OpenOrder =>
        (orders ? openOrder).mapTo[CommandError \/ Response]
      case command: OrderCommand[Response] =>
        (orders ? command).mapTo[CommandError \/
          Response] //TODO these casts are wrong
      case _ => Future.successful(new CommandError {}.left)
    }
  }

  override def handle[Response](query: Query[Response]): Future[Exception \/
    Response] = {
    query match {
      case GetActiveOrders =>
        (ordersView ? query).mapTo[Exception \/ Response]
      case getOrderDetails: GetOrderDetails =>
        (orderDetails ? getOrderDetails).mapTo[Exception \/
          Response] //TODO these casts are wrong
    }
  }
}