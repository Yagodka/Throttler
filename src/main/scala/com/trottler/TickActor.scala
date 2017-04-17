package com.trottler

import akka.actor.{Actor, ActorRef, Props}

sealed trait TickMessage

case object ResetCounters extends TickMessage

object TickActor {
  def props(throttler: ActorRef): Props = Props(new TickActor(throttler))
}

class TickActor(val throttler: ActorRef) extends Actor {

  def receive = {
    case ResetCounters => throttler ! ResetCounters
    case _ => unhandled()
  }
}