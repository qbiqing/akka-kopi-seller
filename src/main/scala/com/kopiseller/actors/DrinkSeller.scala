package com.kopiseller.actors

import akka.actor.{Actor, Props}

object DrinkSeller {
  def props(name: String): Props = Props(new DrinkSeller(name))

  /**
   * Messages
   */
  final case class Add(cups: Int)
  final case class Buy(cups: Int)
  final case object GetDrinkCount
  final case object Clear
}

class DrinkSeller(name: String) extends Actor {
  import DrinkSeller._

  var count = 0

  def receive: Receive = {
    case Add(newCups) => count += newCups

    case Buy(cups) =>
      if (cups <= count) {
        sender() ! cups
        count -= cups
      }
      else sender() ! 0

    case GetDrinkCount => sender() ! count
    case Clear => count = 0
  }
}
