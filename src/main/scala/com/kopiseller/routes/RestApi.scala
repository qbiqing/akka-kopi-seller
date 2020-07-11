package com.kopiseller.routes

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.pattern.ask
import akka.util.Timeout
import com.kopiseller.actors.ShopOwner.EventResponse
import com.kopiseller.actors._
import com.kopiseller.{Drink, Error, EventMarshaller, MakeCoffee}
import org.slf4j.LoggerFactory


class RestApi(system: ActorSystem, timeout: Timeout) extends RestRoutes {
  implicit val requestTimeout: Timeout = timeout
  implicit def executionContext: ExecutionContextExecutor = system.dispatcher

  def createShop(): ActorRef = system.actorOf(ShopOwner.props)
}

trait RestRoutes extends KopiShopApi with EventMarshaller {
  val service = "kopi-seller"
  val logger = LoggerFactory.getLogger(classOf[RestApi])

  protected val createSellerRoute: Route =
    pathPrefix(service / "create") {
      post {
        pathEndOrSingleSlash {
          entity(as[Drink]) { t =>
            onSuccess(createSeller(t.name)) {
              case ShopOwner.DrinkCreated(name) =>
                complete(Created, s"$name created")
              case ShopOwner.DrinkExists(name) =>
                val err = Error(s"$name seller already exists!")
                complete(BadRequest, err)
              case _ => complete(BadRequest, Error("bad request"))
            }
          }
        }
    }
  }

  protected val startSellersRoute: Route =
    pathPrefix(service / "make") {
      post {
        pathEndOrSingleSlash {
          entity(as[MakeCoffee]) { t =>
            onSuccess(makeCoffee(t.cups)) {
              case ShopOwner.MadeCoffee() =>
                complete(s"started making coffee")
              case _ => complete(InternalServerError, Error("unable to start"))
            }
          }
        }
      }
    }
  val routes: Route = createSellerRoute ~ startSellersRoute
}

trait KopiShopApi {

  def createShop(): ActorRef

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  lazy val kopiShop: ActorRef = createShop()

  def createSeller(name: String): Future[EventResponse] = {
    kopiShop.ask(ShopOwner.CreateDrink(name))
      .mapTo[EventResponse]
  }

  def makeCoffee(cups: Int): Future[EventResponse] = {
    kopiShop.ask(ShopOwner.MakeCoffee(cups)).mapTo[EventResponse]
  }

  def getAvailableCoffee(seller: String): Future[Option[Int]] =
    kopiShop.ask(ShopOwner.GetCount()).mapTo[Option[Int]]

}