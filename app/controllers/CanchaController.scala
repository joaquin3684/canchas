package controllers

import javax.inject.{Inject, Singleton}

import models.Cancha
import play.api.mvc._
import repositories.CanchaRepository
import schemas.Schemas.Canchas

@Singleton
class CanchaController @Inject()(cc: ControllerComponents, val repo: CanchaRepository) extends AllAbmController[Cancha, Canchas, CanchaRepository](cc) {

}
