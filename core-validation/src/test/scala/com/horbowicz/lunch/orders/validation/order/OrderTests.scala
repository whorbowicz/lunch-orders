package com.horbowicz.lunch.orders.validation.order

import java.time.LocalTime

import akka.actor.ActorSystem
import akka.persistence.inmemory.query.journal.scaladsl.InMemoryReadJournal
import akka.persistence.query.PersistenceQuery
import com.horbowicz.lunch.orders._
import com.horbowicz.lunch.orders.command.order.{AddOrderItem, OpenOrder}
import com.horbowicz.lunch.orders.query.order.{GetActiveOrders, GetOrderDetails}
import com.horbowicz.lunch.orders.read.order._
import com.horbowicz.lunch.orders.validation.ValidationTest
import org.scalatest.BeforeAndAfter

import scala.collection.immutable.Seq
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scalaz._

class OrderTests extends ValidationTest with BeforeAndAfter {

  private var system: LunchOrderSystem = _

  before {
    val actorSystem = ActorSystem.apply("lunch-orders-core-validation-tests")
    val readJournal = PersistenceQuery(actorSystem)
      .readJournalFor[InMemoryReadJournal](InMemoryReadJournal.Identifier)
    system = new AkkaLunchOrderSystem(actorSystem, readJournal)
  }

  "As a user I want ot be able to" - {
    """open new order for current day with following information:
      | * provider
      | * ordering time
      | * expected delivery time
      | """.stripMargin in {
      val operationResult = openOrder as "WHO" from "Food House" orderedAt
        LocalTime.of(10, 30) expectingDeliveryAt LocalTime.of(12, 0)
      operationResult must be('right)
    }

    "open new order with a future order and delivery dates" ignore()

    "open new order with order date different that delivery date" ignore()

    "update details of the order that I have opened" ignore()

    "lock order that that I have opened" ignore()

    "unlock (re-open) order that that I have locked" ignore()

    "mark the order that that I have opened as ordered" ignore()

    "update expected delivery time when marking the order as ordered" ignore()

    "list all active (open, locked, ordered) orders" in {
      listOrders must be('empty)
      val \/-(orderId) = openOrder as "WHO" from "Food House" orderedAt
        LocalTime.of(10, 30) expectingDeliveryAt LocalTime.of(12, 0)

      listOrders mustBe Seq(OrdersView.Order(orderId, "Open", "WHO"))
    }

    """add an item to any open order with following information
      | * description
      | * price
      | """.stripMargin in {
      val \/-(orderId) = openOrder as "WHO" from "Food House" orderedAt
        LocalTime.of(10, 30) expectingDeliveryAt LocalTime.of(12, 0)
      val operationResult = addOrderItem as "HBO" toOrder
        orderId withDescription "Meat dumplings, salad" `for` "15.50"
      operationResult must be('right)
    }

    "access order details along with list of it's items" in {
      val \/-(orderId) = openOrder as "WHO" from "Food House" orderedAt
        LocalTime.of(10, 30) expectingDeliveryAt LocalTime.of(12, 0)
      addOrderItem as "HBO" toOrder orderId withDescription
        "Meat dumplings, salad" `for` "15.50"
      addOrderItem as "MAT" toOrder orderId withDescription
        "Spahetti bolognese" `for` "12.00"

      orderDetails of orderId mustBe OrderDetails.Order(
        id = orderId,
        provider = "Food House",
        personResponsible = "WHO",
        orderingTime = LocalTime.of(10, 30),
        status = OrderDetails.Order.Status.Open,
        expectedDeliveryTime = LocalTime.of(12, 0),
        totalPrice = BigDecimal("27.50"),
        items = Seq(
          OrderDetails.OrderItem(
            orderingPerson = "HBO",
            description = "Meat dumplings, salad",
            price = BigDecimal("15.50")
          ),
          OrderDetails.OrderItem(
            orderingPerson = "MAT",
            description = "Spahetti bolognese",
            price = BigDecimal("12.00")
          )
        )
      )
    }

    "edit order item that I have added while the order is opened" ignore()

    "update any item in order I have opened" ignore()
  }

  object openOrder {

    def as(user: String) = new {
      def from(provider: String) = new {
        def orderedAt(orderingTime: LocalTime) = new {
          def expectingDeliveryAt(expectedDeliveryTime: LocalTime) =
            Await.result(
              system.handle(
                OpenOrder(
                  provider,
                  user,
                  orderingTime,
                  expectedDeliveryTime)),
              2 second)
        }
      }
    }
  }

  object addOrderItem {

    def as(user: String) = new {
      def toOrder(orderId: Global.Id) = new {
        def withDescription(description: String) = new {
          def `for`(price: String) =
            Await.result(
              system.handle(
                AddOrderItem(
                  orderId,
                  user,
                  description,
                  BigDecimal(price))),
              10 seconds)
        }
      }
    }
  }

  def listOrders =
    Await.result(
      system.handle(GetActiveOrders),
      10 seconds).toOption.get

  object orderDetails {

    def of(orderId: Global.Id): OrderDetails.Order = {
      Await.result(
        system.handle(GetOrderDetails(orderId)),
        10 seconds).toOption.get
    }
  }

  //
  //"""
  //order lunch:
  //1 announce with closing date
  //2 gather orders (items)
  //3 lock
  //4 verify/amend values
  //5 place
  //
  //"""
}
