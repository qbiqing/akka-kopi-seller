package com.kopiseller.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import akka.pattern.ask
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.kopiseller.actors._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import StatusCodes._
import com.kopiseller.{Error, EventMarshaller, Seller}
import com.kopiseller.actors.ShopOwner.{CreateSeller, EventResponse, GetCountFromSeller}


class RestApi(system: ActorSystem, timeout: Timeout) extends RestRoutes {
  implicit val requestTimeout: Timeout = timeout
  implicit def executionContext: ExecutionContextExecutor = system.dispatcher

  def createShop(): ActorRef = system.actorOf(ShopOwner.props)
}

trait RestRoutes extends KopiShopApi with EventMarshaller {
  val service = "kopi-seller"

  protected val createSellerRoute: Route =
    path(service / "sellers") {
      post {
        entity(as[Seller]) { params =>
          onSuccess(createSeller(params.name)) {
            case ShopOwner.SellerCreated(name) => complete(Created, name)
            case ShopOwner.SellerExists(name) =>
              val err = Error(s"$name seller already exists!")
              complete(BadRequest, err)
            case _ => complete(BadRequest, Error("bad request"))
          }
        }
    }
  }

  val routes: Route = createSellerRoute
}

trait KopiShopApi {

  def createShop(): ActorRef

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  lazy val kopiShop: ActorRef = createShop()

  def createSeller(name: String): Future[EventResponse] = {
    kopiShop.ask(CreateSeller(name))
      .mapTo[EventResponse]
  }

  def getAvailableCoffee(seller: String): Future[Option[Int]] =
    kopiShop.ask(GetCountFromSeller(seller)).mapTo[Option[Int]]

}