package controllers

import javax.inject.{Inject, Singleton}

import models.Lugar
import play.api.libs.json._
import play.api.mvc._
import repositories.LugarRepository
import schemas.Schemas.Lugares

@Singleton
class LugarController @Inject()(cc: ControllerComponents, val repo: LugarRepository) extends AllAbmController[Lugar, Lugares, LugarRepository](cc) {

  implicit val lu = Json.reads[Lugar]
 def nuevo= Action { implicit request =>
   val lugar = JsPath.read(Lugar)
   Ok("")
 }
}
