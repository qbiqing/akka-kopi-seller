package com.kopiseller

import de.heikoseeberger.akkahttpplayjson._
import play.api.libs.json._

case class Drink(name: String)
case class MakeCoffee(cups: Int)
case class BuyCoffee(name: String, cups: Int)

case class Error(message: String)

trait EventMarshaller extends PlayJsonSupport {
  implicit val drinkFormat: OFormat[Drink] = Json.format[Drink]
  implicit val makeCoffeeFormat: OFormat[MakeCoffee] = Json.format[MakeCoffee]
  implicit val buyCoffeeFormat: OFormat[BuyCoffee] = Json.format[BuyCoffee]

  implicit val errorFormat: OFormat[Error] = Json.format[Error]
}

object EventMarshaller extends EventMarshaller
