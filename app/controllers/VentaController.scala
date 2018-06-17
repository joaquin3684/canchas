package controllers

import javax.inject.Inject

import actions.{AuthenticatedAction, GetAuthenticatedAction, JsonMapperAction}
import akka.http.scaladsl.model.DateTime
import models.Venta
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.VentaRepository
import services.JsonMapper

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class VentaController @Inject()(cc: ControllerComponents, val jsonMapper: JsonMapper, val authAction: AuthenticatedAction, val getAuthAction: GetAuthenticatedAction) extends AbstractController(cc){

  def create = authAction { implicit request =>

    val userName = jsonMapper.getAndRemoveElement(request.rootNode, "user")
    val f = jsonMapper.getAndRemoveElement(request.rootNode, "fechaCreacion")
    val fechaCreacion = DateTime.fromIsoDateTimeString(f).get
    val ventasJson = request.rootNode.toString
    val venta = jsonMapper.fromJson[Venta](ventasJson)
    if(request.obrasSociales.contains(venta.idObraSocial)) {
      val futureVenta = VentaRepository.create(venta, userName, fechaCreacion)
      Await.result(futureVenta, Duration.Inf)
      Ok("creado")
    } else throw new RuntimeException("obra social erronea")
  }

  def all = getAuthAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val futureVentas = VentaRepository.all(request.user)
    val ventas = Await.result(futureVentas, Duration.Inf)
    val json = jsonMapper.toJson(ventas)
    Ok(json)

  }
}
