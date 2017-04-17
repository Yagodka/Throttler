package com.trottler

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.Logging
import akka.stream.ActorMaterializer

import scala.io.StdIn
import scala.concurrent.duration._

object Main extends App {

  val host = "localhost"
  val port = 8080
  val graceRps = 50
  val slaDb = Map("dXNlcjAxOnBzdzAx" -> Sla("user01", 80))

  implicit val system = ActorSystem("trottler")
  implicit val materializer = ActorMaterializer()
  implicit val exec = system.dispatcher

  val log = Logging(system, Main.getClass)

  val slaService: ActorRef = system.actorOf(Props(new SlaService(slaDb)), "SlaService")
  val throttlingService: ActorRef = system.actorOf(ThrottlingService.props(graceRps, slaService), "ThrottlingService")
  val ticker: ActorRef = system.actorOf(TickActor.props(throttlingService), "Ticker")
  system.scheduler.schedule(0 second, 1 second, ticker, ResetCounters)

  val bindingFuture = Server(slaService, throttlingService, ticker).start(host, port)

  log.info("Press RETURN to stop...")
  StdIn.readLine()

  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}