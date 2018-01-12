package controllers

import javax.inject.Inject

import models.Usuario
import play.api.mvc.ControllerComponents
import repositories.UsuarioRepository
import schemas.Schemas.Usuarios

class UsuarioController @Inject()(cc: ControllerComponents, val repo: UsuarioRepository) extends AllAbmController[Usuario, Usuarios, UsuarioRepository](cc){

}
