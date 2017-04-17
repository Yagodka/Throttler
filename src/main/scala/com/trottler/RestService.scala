package com.trottler

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.Authorization
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}


class RestService(val slaService: ActorRef,
                  val throttlingService: ActorRef,
                  val ticker: ActorRef)(
                   implicit val system: ActorSystem,
                   implicit val materializer: ActorMaterializer,
                   implicit val exec: ExecutionContext) {

  implicit val timeout = Timeout(1 second)

  def createResponse(token: Option[String]): Future[Boolean] =
    (throttlingService ? IsRequestAllowed(token)).mapTo[Boolean]

  def credentialsOfRequest(req: HttpRequest): Option[String] =
    for {
      Authorization(credentials) <- req.header[Authorization]
      token = credentials.token()
    } yield token

  val route: Route =
    get {
      path("service") {
        extractRequest { request =>
          val token: Option[String] = credentialsOfRequest(request)
          val responseFuture = createResponse(token)

          onComplete(responseFuture) {
            case Success(true) => complete(OK)
            case Success(false) => complete(BandwidthLimitExceeded -> "Quota exceeded")
            case Failure(err) => complete(InternalServerError -> s"An error occurred: ${err.getMessage}")
          }
        }
      } ~
        path("service_without_throttle") {
          extractRequest { _ =>
            complete(OK)
          }
        }
    } ~
      put {
        path("set") {
          parameter('graceRps.as[Int]) { graceRps =>
            throttlingService ! SetGraceRps(graceRps)
            complete(OK)
          }
        }
      }
}
