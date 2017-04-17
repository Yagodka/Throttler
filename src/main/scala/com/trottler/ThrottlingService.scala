package com.trottler

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.util.{Failure, Success}
import scala.concurrent.duration._

sealed trait ThrottlingServiceMessage

case class IsRequestAllowed(token: Option[String]) extends ThrottlingServiceMessage

case class SetGraceRps(graceRps: Int) extends ThrottlingServiceMessage

object ThrottlingService {
  def props(graceRps: Int, slaService: ActorRef): Props = Props(new ThrottlingService(graceRps, slaService))
}

class ThrottlingService(var graceRps: Int,
                        val slaService: ActorRef) extends Actor with ActorLogging {

  val unauthorized = "Unauthorized"

  implicit val timeout = Timeout(1 second)
  implicit val exec = context.dispatcher

  private val cashSla = collection.mutable.Map[String, Sla]()
  private var counters = collection.mutable.Map[String, Int]()

  def receive: Receive = {
    case IsRequestAllowed(token) =>
      val isAllowed = isRequestAllowed(token)
      log.debug(s"Request is allowed: $isAllowed")
      sender() ! isAllowed
    case SetGraceRps(newGraceRps) =>
      log.debug(s"graceRps set to: $newGraceRps")
      graceRps = newGraceRps
    case ResetCounters => resetCounters()
    case _ => unhandled()
  }

  def isRequestAllowed(token: Option[String]): Boolean = token match {
    case Some(t) =>
      log.debug(s"Token $t is defined")
      cashSla.get(t) match {
        case Some(Sla(user, rps)) =>
          log.debug(s"Answer from SLA service cashed: $user $rps")
          isAllowed(user, rps)
        case None =>
          log.debug(s"Answer from SLA service not cashed yet")
          val requestSla = GetSlaByToken(t)
          log.debug(s"Request to SLA service $requestSla")
          val slaFuture = slaService ? requestSla
          cashSla.put(t, Sla(unauthorized, graceRps))
          slaFuture.mapTo[Option[Sla]].onComplete {
            case Success(Some(newSla)) =>
              log.debug(s"Answer $newSla from SLA cashed")
              cashSla.put(t, newSla)
            case Success(None) => log.debug(s"User for token $t not found. User not authorized")
            case Failure(err) => log.error(err, s"User not authorized")
          }
          log.debug(s"User Unauthorized")
          isAllowed(unauthorized, graceRps)
      }
    case None =>
      log.debug(s"Token is not defined. User Unauthorized")
      isAllowed(unauthorized, graceRps)
  }

  private def isAllowed(user: String, maxRps: Int): Boolean = {
    val count = counters.getOrElse(user, 0)
    if (count < maxRps) {
      log.debug(s"User $user, count " + (count + 1))
      counters.put(user, count + 1)
      log.debug(s"Request allowed")
      true
    }
    else {
      log.debug(s"Limit RPS exceeded $user, count $count, rps $maxRps. Request not allowed")
      false
    }
  }

  private def resetCounters() =
    counters = collection.mutable.Map(counters.keySet.map(k => (k, 0)).toSeq: _*)
}