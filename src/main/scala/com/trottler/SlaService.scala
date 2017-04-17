package com.trottler

import akka.actor.{Actor, ActorLogging, Props}

sealed trait SlaServiceMessage
case class GetSlaByToken(token: String) extends SlaServiceMessage
case class Sla(user: String, rps: Int)

object SlaService {
  def props: Props = Props(new SlaService())
}

class SlaService(val data: Map[String, Sla] = Map()) extends Actor with ActorLogging {

  def getSlaByToken(token: String): Option[Sla] = {
    Thread.sleep(250)
    data.get(token)
  }

  override def receive: Receive = {
    case GetSlaByToken(token) => sender ! getSlaByToken(token)
  }
}