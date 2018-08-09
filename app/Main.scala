

import java.sql.Timestamp

import JsonFormats.{DateTimeDeserializer, DateTimeSerializer}
import akka.http.scaladsl.model.DateTime
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.module.SimpleModule
import models._
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.Success
import com.github.t3hnar.bcrypt._
import repositories.Db
import schemas.Schemas.{estados, usuariosObrasSociales, ventas, visitas}
import services.JsonMapper
import slick.jdbc.GetResult

object Main extends App {

  val pat = "(?<=-)(OK|RP|RT|OB)(?=-)".r
  val str = "CLAUDIO MANURP-AM-OK-1.mp3"
  println(pat findFirstIn str)


}


