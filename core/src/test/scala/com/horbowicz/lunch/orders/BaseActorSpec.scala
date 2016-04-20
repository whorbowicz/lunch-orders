package com.horbowicz.lunch.orders

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll

class BaseActorSpec(actorSystem: ActorSystem)
  extends TestKit(actorSystem)
    with ImplicitSender
    with BaseSpec
    with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }
}
