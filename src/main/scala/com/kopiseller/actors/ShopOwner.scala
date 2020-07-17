package com.kopiseller.actors

import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout

object ShopOwner {

  def props()(implicit timeout: Timeout): Props = Props(new ShopOwner())

  /**
   * Messages
   */
  final case class MakeCoffee(cups: Int)
  final case class GetCount(name: String)
  final case class ClearCount()
  final case class CreateDrink(name: String)
  final case class BuyDrink(name: String, cups: Int)

  sealed trait EventResponse
  final case class DrinkCreated(name: String) extends EventResponse
  final case class DrinkNotFound(name: String) extends EventResponse
  final case class DrinkExists(name: String) extends EventResponse
  final case class MadeCoffee() extends EventResponse
  final case class ClearedCount() extends EventResponse
}

class ShopOwner(implicit timeout: Timeout) extends Actor {
  import ShopOwner._

  /** DrinkSeller child */
  def createDrink(name: String): ActorRef = {
    context.actorOf(DrinkSeller.props(name), name)
  }

  def receive: Receive = {
    case CreateDrink(name) =>
      def create(): Unit = {
        createDrink(name)
        sender() ! DrinkCreated(name)
      }
      // If already exists
      context.child(name).fold(create())(_ => sender() ! DrinkExists(name))

    case MakeCoffee(cups) =>
      for (_ <- 1 to cups) {
        makeCoffee()
      }
      sender() ! MadeCoffee()

    case BuyDrink(name, cups) =>
      def notFound(): Unit =
        sender() ! DrinkNotFound(name)
      def buy(child: ActorRef): Unit =
        child.forward(DrinkSeller.Buy(cups))
      context.child(name).fold(notFound())(buy)

    case GetCount(name) =>
      def notFound(): Unit =
        sender() ! DrinkNotFound(name)
      def getCount(child: ActorRef): Unit =
        child.forward(DrinkSeller.GetDrinkCount)
      context.child(name).fold(notFound())(getCount)

    case ClearCount() =>
      def clear(child: ActorRef): Unit =
        child.forward(DrinkSeller.Clear)
      context.children.foreach {
        child => clear(child)
      }
      sender() ! ClearedCount()
  }

  private def makeCoffee(): Unit = {
    for (x <- context.children) yield {
      x ! DrinkSeller.Add(1)
    }
  }
}
