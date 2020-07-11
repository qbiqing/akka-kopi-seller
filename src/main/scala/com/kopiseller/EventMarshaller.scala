package com.kopiseller

import de.heikoseeberger.akkahttpplayjson._
import play.api.libs.json._

case class CoffeeCount(cups: Int) {
  require(cups > 0)
}

case class Drink(name: String)
case class Error(message: String)
case class MakeCoffee(cups: Int)

trait EventMarshaller extends PlayJsonSupport {
  implicit val coffeeCountFormat: OFormat[CoffeeCount] = Json.format[CoffeeCount]
  implicit val sellerFormat: OFormat[Drink] = Json.format[Drink]
  implicit val errorFormat: OFormat[Error] = Json.format[Error]
  implicit val makeCoffeeFormat: OFormat[MakeCoffee] = Json.format[MakeCoffee]
}

object EventMarshaller extends EventMarshaller
