package com.horbowicz.lunch.orders

import akka.actor.PoisonPill
import akka.persistence.inmemory.query.journal.scaladsl.InMemoryReadJournal
import akka.persistence.query.PersistenceQuery
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterEach, Suite}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

trait EventsListener extends BeforeAndAfterEach {
  self: Suite with TestKit =>

  def persistenceId: String

  private implicit val mat = ActorMaterializer()(system)
  private lazy val readJournal = PersistenceQuery(system)
    .readJournalFor[InMemoryReadJournal](InMemoryReadJournal.Identifier)

  private var listener: TestProbe = _

  def eventsListener = listener

  override protected def beforeEach(): Unit = {
    listener = TestProbe()

    readJournal
      .eventsByPersistenceId(persistenceId, 0L, Long.MaxValue)
      .map(_.event)
      .runWith(Sink.actorRef(listener.ref, PoisonPill))
    super.beforeEach()
  }

  override protected def afterEach(): Unit = {
    try super.afterEach()
    finally {
      listener.ref ! PoisonPill
      Await
        .result(
          readJournal.journalDao.delete(persistenceId, Int.MaxValue),
          1 second)
    }
  }
}
