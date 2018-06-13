package actions

import javax.inject.Inject

import play.api.mvc._
import repositories.UsuarioRepository
import requests.UserRequest
import services.JsonMapper
import pdi.jwt.JwtSession

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

case class AuthenticatedAction @Inject()(val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext)
  extends ActionBuilder[UserRequest, AnyContent] with ActionTransformer[Request, UserRequest] {

  def transform[A](request: Request[A]) = Future.successful {

    val jsonMapper = new JsonMapper
    val json = request.body.asInstanceOf[AnyContentAsJson].asJson.get.toString
    val rootNode = jsonMapper.getJsonNode(json)

    val token = request.headers.get("My-Authorization").get.split("Bearer ") match {
      case Array(_, tok) => Some(tok)
      case _ => None
    }
    var session = JwtSession.deserialize(token.get)

    val path = request.path
    val pantalla = path.split("/")(1)
    val userId = session.get("user_id").get.as[String]
    val pantallas = session.get("permisos").get.as[Seq[String]]
    val obrasSociales = session.get("obrasSociales").get.as[Seq[String]]
    if(pantallas.contains(pantalla)) new UserRequest(obrasSociales, rootNode, json, userId, request) else {
      val userRepo = new UsuarioRepository
      val futureRuta = userRepo.getRuta(path, pantallas)
      val ruta = Await.result(futureRuta, Duration.Inf)
      if (ruta.isEmpty) throw new RuntimeException("no tiene permiso para esta ruta") else new UserRequest(obrasSociales, rootNode, json, userId, request)
    }
    }


}
