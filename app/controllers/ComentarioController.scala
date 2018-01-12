package controllers

import javax.inject.Inject

import models.Comentario
import play.api.mvc.ControllerComponents
import repositories.ComentarioRepository
import schemas.Schemas.Comentarios

class ComentarioController @Inject()(cc: ControllerComponents, val repo: ComentarioRepository) extends AllAbmController[Comentario, Comentarios, ComentarioRepository](cc){

}
