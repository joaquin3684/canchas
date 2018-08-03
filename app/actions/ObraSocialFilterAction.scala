package actions

import javax.inject.Inject

import akka.http.scaladsl.model.DateTime
import play.api.Logger
import play.api.mvc._
import repositories.VentaRepository
import requests.UserRequest

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import play.api.mvc.Results.Forbidden

case class ObraSocialFilterAction @Inject()(parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext)
  extends ActionRefiner[UserRequest, UserRequest] with ActionFilter[UserRequest] {

  def filter[A](request: UserRequest[A]) = Future.successful {
    implicit val obs: Seq[String] = request.obrasSociales
    val metodo = request.method
    val idVenta = if(metodo == "PUT") request.uri.split("/").last.toInt else request.rootNode.get("idVenta").asLong
    val futureCheckObs = VentaRepository.checkObraSocial(idVenta)
    val check = Await.result(futureCheckObs, Duration.Inf)
    if (check.nonEmpty)
      None
    else {
      Logger.error("intento de acceder a un elemento para el cual no tiene permiso. usuario: " + request.user + " fecha: " + DateTime.now.toIsoDateTimeString())
      Some(Forbidden("unouthorized"))
    }
  }
}
