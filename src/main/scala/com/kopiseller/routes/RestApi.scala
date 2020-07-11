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
import com.kopiseller._
import org.slf4j.LoggerFactory


class RestApi(system: ActorSystem, timeout: Timeout) extends RestRoutes {
  implicit val requestTimeout: Timeout = timeout
  implicit def executionContext: ExecutionContextExecutor = system.dispatcher

  def createShop(): ActorRef = system.actorOf(ShopOwner.props)
}

trait RestRoutes extends KopiShopApi with EventMarshaller {
  val service = "kopi-seller"
  val logger = LoggerFactory.getLogger(classOf[RestApi])

  protected val createDrinkRoute: Route =
    pathPrefix(service / "create") {
      post {
        pathEndOrSingleSlash {
          entity(as[Drink]) { t =>
            onSuccess(createDrink(t.name)) {
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

  protected val makeCoffeeRoute: Route =
    pathPrefix(service / "make") {
      post {
        pathEndOrSingleSlash {
          entity(as[MakeCoffee]) { t =>
            onSuccess(makeCoffee(t.cups)) {
              case ShopOwner.MadeCoffee() =>
                complete(OK, s"made ${t.cups} cups of each coffee")
              case _ => complete(InternalServerError, Error("unable to make"))
            }
          }
        }
      }
    }

  protected val getCountRoute: Route =
    pathPrefix(service / "get-count"){
      get {
        pathEndOrSingleSlash {
          entity(as[Drink]) { t =>
            onSuccess(getAvailableCount(t.name)) { count =>
                complete(OK, s"$count cups of coffee available")
            }
          }
        }
      }
    }

  protected val buyCoffeeRoute: Route =
    pathPrefix(service / "buy"){
      post {
        pathEndOrSingleSlash {
          entity(as[BuyCoffee]) { t =>
            onSuccess(buyCoffee(t.name, t.cups)) { count =>
              complete(OK, s"bought $count cups of coffee")
            }
          }
        }
      }
    }

  protected val clearCountsRoute: Route =
    pathPrefix(service / "clear"){
      post {
        pathEndOrSingleSlash {
          onSuccess(clearCounts()) {
            case ShopOwner.ClearedCount() => complete(OK, "cleared all counts!")
            case _ => complete(InternalServerError, Error("unable to clear"))
          }
        }
      }
    }

  val routes: Route = createDrinkRoute ~ makeCoffeeRoute ~ getCountRoute ~ buyCoffeeRoute ~ clearCountsRoute
}

trait KopiShopApi {

  def createShop(): ActorRef

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  lazy val kopiShop: ActorRef = createShop()

  def createDrink(name: String): Future[EventResponse] = {
    kopiShop.ask(ShopOwner.CreateDrink(name))
      .mapTo[EventResponse]
  }

  def makeCoffee(cups: Int): Future[EventResponse] = {
    kopiShop.ask(ShopOwner.MakeCoffee(cups)).mapTo[EventResponse]
  }

  def getAvailableCount(name: String): Future[Int] =
    kopiShop.ask(ShopOwner.GetCount(name)).mapTo[Int]

  def buyCoffee(name: String, cups: Int): Future[Int] =
    kopiShop.ask(ShopOwner.BuyDrink(name, cups)).mapTo[Int]

  def clearCounts(): Future[EventResponse] =
    kopiShop.ask(ShopOwner.ClearCount()).mapTo[EventResponse]
}