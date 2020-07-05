package com.kopiseller.actors

import akka.actor.{Actor, Props}

object KopiSeller {
  def props(name: String): Props = Props(new KopiSeller(name))

  /**
   * Messages
   */
  final case class Add(cups: Int)
  final case class Buy(cups: Int)
  final case object GetCount
  final case object Clear
}

class KopiSeller(name: String) extends Actor {
  import KopiSeller._

  var coffeeCount = 0

  def receive: Receive = {
    case Add(newCups) => coffeeCount += newCups

    case Buy(cups) =>
      if (cups <= coffeeCount) {
        sender() ! cups
        coffeeCount -= cups
      }
      else sender() ! 0

    case GetCount => sender() ! coffeeCount
    case Clear => coffeeCount = 0
  }
}
