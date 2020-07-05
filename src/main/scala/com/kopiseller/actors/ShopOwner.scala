package com.kopiseller.actors

import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout

object ShopOwner {

  def props()(implicit timeout: Timeout): Props = Props(new ShopOwner())

  /**
   * Messages
   */
  final case class Start(maxLimit: Int)
  final case object Stop

  final case class CreateSeller(name: String)
  final case class BuyFromSeller(name: String, cups: Int)
  final case class GetCountFromSeller(name: String)
  final case class ClearSellerCount(name: String)

  sealed trait EventResponse
  final case class SellerNotFound(name: String) extends EventResponse
  final case class SellerCreated(name: String) extends EventResponse
  final case class SellerExists(name: String) extends EventResponse
}

class ShopOwner(implicit timeout: Timeout) extends Actor {
  import ShopOwner._

  /** KopiSeller child */
  def createKopiSeller(name: String): ActorRef = {
    context.actorOf(KopiSeller.props(name), name)
  }

  val sellers: List[ActorRef] = List()
  var started: Boolean = false

  def receive: Receive = {
    case CreateSeller(name) =>
      def create(): Unit = {
        sellers :+ createKopiSeller(name)
        sender() ! SellerCreated(name)
      }
      // If already exists
      context.child(name).fold(create())(_ ⇒ sender() ! SellerExists)

    case Start(maxLimit) =>
      started = true

      for (_ <- 0 to maxLimit) {
        if (started) makeCoffee()
      }

    case Stop => started = false

    case BuyFromSeller(name, cups) =>
      def notFound(): Unit =
        sender() ! SellerNotFound(name)
      def buy(child: ActorRef): Unit =
        child.forward(KopiSeller.Buy(cups))
      context.child(name).fold(notFound())(buy)

    case GetCountFromSeller(name: String) =>
      def notFound(): Unit =
        sender() ! SellerNotFound(name)
      def getCount(child: ActorRef): Unit =
        child.forward(KopiSeller.GetCount)
      sender() ! context.child(name).fold(notFound())(getCount)

    case ClearSellerCount(name: String) =>
      def notFound(): Unit =
        sender() ! SellerNotFound(name)
      def clear(child: ActorRef): Unit =
        child.forward(KopiSeller.Clear)
      context.child(name).fold(notFound())(clear)
  }

  private def makeCoffee(): Unit = {
    for (x <- sellers) yield {
      x ! KopiSeller.Add(1)
    }
    Thread.sleep(1000)
  }
}
