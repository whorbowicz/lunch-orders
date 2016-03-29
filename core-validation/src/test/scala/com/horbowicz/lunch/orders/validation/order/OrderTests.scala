package com.horbowicz.lunch.orders.validation.order

import java.time.LocalTime

import com.horbowicz.lunch.orders.LunchOrderSystem
import com.horbowicz.lunch.orders.AkkaLunchOrderSystem
import com.horbowicz.lunch.orders.command.order.OpenOrder
import com.horbowicz.lunch.orders.validation.ValidationTest

import scalaz._
import Scalaz._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class OrderTests extends ValidationTest {
  val system: LunchOrderSystem = new AkkaLunchOrderSystem

  "As a user I want ot be able to" - {
    """open new order for current day with following information:
    | * provider
    | * ordering time
    | * expected delivery time
    |""".stripMargin ignore {
      val operationResult = openOrder as "WHO" from "Food House" orderedAt
        LocalTime.of(10, 30) expectingDeliveryAt LocalTime.of(12, 0)
      operationResult must be ('right)
    }

    "open new order with a future date" ignore()

    "update details of the order that I have opened" ignore()

    "lock order that that I have opened" ignore()

    "unlock (re-open) order that that I have locked" ignore()

    "mark the order that that I have opened as ordered" ignore()

    "update expected delivery time when marking the order as ordered" ignore()

    "list all active (open, locked, ordered) orders" ignore()

    """add an item to any open order with following information
    | * description
    | * price
    |""".stripMargin ignore()

    "edit order item that I have added while the order is opened" ignore()

    "update any item in order I have opened" ignore()
  }

  object openOrder {
    def as(user: String) = new {
      def from(provider: String) = new {
        def orderedAt(orderingTime: LocalTime) = new {
          def expectingDeliveryAt(expectedDeliveryTime: LocalTime) =
            Await.result(system.handle(OpenOrder(provider, user, orderingTime, expectedDeliveryTime)), 1 second)
        }
      }
    }
  }
}
