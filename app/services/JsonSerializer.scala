package services

import models.BaseEntity
import play.api.libs.json._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.util.parsing.input.Reader

class JsonSerializer {

    def deserealizar[T <: BaseEntity](json: JsValue)(implicit r: Reads[T]) : Try[T] = {

      Json.fromJson[T](json) match {
        case JsSuccess(value: T, path:JsPath) => Success(value)
        case e: JsError => Failure(new RuntimeException("alsdkfjalsdj"))
      }
    }

    def serealizar[T <: BaseEntity](entity: T)(implicit r: Writes[T]) : JsValue = Json.toJson(entity)

    def serealizar[T <: BaseEntity](seq: Seq[T])(implicit r: Writes[T]) : JsValue = Json.toJson(seq)
}
