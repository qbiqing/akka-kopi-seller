package com.kopiseller.actors

import akka.actor.Actor
import com.kopiseller.actors.ShopOwner.Coffee

object KopiSeller {
  /**
   * Messages
   */
  final case class Add(cups: Int)
  final case class Buy(cups: Int)
  final case object GetAvailable
  final case object Clear
}

class KopiSeller() extends Actor {
  import KopiSeller._

  var coffeeCount = 0

  def receive: Receive = {
    case Add(newCups) => coffeeCount += newCups

    case Buy(cups) =>
      if (cups <= coffeeCount) {
        sender() ! Coffee(cups)
        coffeeCount -= cups
      }
      else sender() ! Coffee(0)

    case GetAvailable => sender() ! coffeeCount
    case Clear => coffeeCount = 0
  }
}
