package com.kopiseller

import play.api.libs.json._
import de.heikoseeberger.akkahttpplayjson._

case class CoffeeCount(cups: Int) {
  require(cups > 0)
}

case class Seller(name: String)
case class Error(message: String)

trait EventMarshaller extends PlayJsonSupport {
  implicit val coffeeCountFormat: OFormat[CoffeeCount] = Json.format[CoffeeCount]
  implicit val sellerFormat: OFormat[Seller] = Json.format[Seller]
  implicit val errorFormat: OFormat[Error] = Json.format[Error]


}

object EventMarshaller extends EventMarshaller
