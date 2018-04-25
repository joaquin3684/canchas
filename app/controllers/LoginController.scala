package controllers

import javax.inject.Inject

import actions.JsonMapperAction
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.UsuarioRepository
import services.JsonMapper
import com.github.t3hnar.bcrypt._
import pdi.jwt.JwtSession
import play.api.libs.json._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class LoginController @Inject()(cc: ControllerComponents, val jsonMapper: JsonMapper, val jsonMapperAction: JsonMapperAction, val userRepo: UsuarioRepository) extends AbstractController(cc){


  def login = jsonMapperAction { implicit request =>

    val rootNode = request.rootNode
    val user = rootNode.get("user").asText()
    val password = rootNode.get("password").asText()

    val futureUser = userRepo.validateCredentials(user)
    val result = Await.result(futureUser, Duration.Inf)

    val optionUser = result.map(_._1).headOption


    val authUser = if(optionUser.nonEmpty && password.isBcrypted(optionUser.get.password)) optionUser.get else throw new RuntimeException("credenciales erroneas")

    val obs = result.map(_._2.nombre).distinct
    val pantallas = result.map(_._3.nombre).distinct


    var session = JwtSession.apply(Json.obj("user_id" -> authUser.user, "obrasSociales" -> obs, "permisos" -> pantallas))
    val token = session.serialize

    Ok(token)
  }
}
