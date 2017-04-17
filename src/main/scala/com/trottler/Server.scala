package com.trottler

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext

case class Server(override val slaService: ActorRef,
                  override val throttlingService: ActorRef,
                  override val ticker: ActorRef)(
                  implicit override val system: ActorSystem,
                  implicit override val materializer: ActorMaterializer,
                  implicit override val exec: ExecutionContext)
                        extends RestService(slaService, throttlingService, ticker) {

  val log = Logging(system, this.getClass)

  def start(host: String, port: Int) = {
    log.info(s"Server online at http://$host:$port")
    Http().bindAndHandle(route, host, port)
  }
}