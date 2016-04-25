package com.horbowicz.lunch.orders

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest._

import scala.concurrent.duration._
import scala.language.postfixOps

class BaseActorSpec(actorSystem: ActorSystem)
  extends TestKit(actorSystem)
    with ImplicitSender
    with BaseSpec
    with BeforeAndAfterAll {

  override def afterAll: Unit = {
    try super.afterAll
    finally TestKit.shutdownActorSystem(system)
  }

  val defaultDuration = 1 second
}
