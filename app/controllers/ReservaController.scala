package controllers

import javax.inject.Inject

import models.Reserva
import play.api.mvc.ControllerComponents
import repositories.ReservaRepository
import schemas.Schemas.Reservas

class ReservaController @Inject()(cc: ControllerComponents, val repo: ReservaRepository) extends AllAbmController[Reserva, Reservas, ReservaRepository](cc){

}
