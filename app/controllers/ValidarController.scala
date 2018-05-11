package controllers

import javax.inject.Inject

import actions.{AuthenticatedAction, GetAuthenticatedAction, JsonMapperAction, ObraSocialFilterAction}
import models.Venta
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.{ValidacionRepository, VentaRepository}
import services.JsonMapper

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ValidarController @Inject()(cc: ControllerComponents, val valiRepo: ValidacionRepository, val jsonMapper: JsonMapper, jsonMapperAction: JsonMapperAction, authAction: AuthenticatedAction, val getAuthAction: GetAuthenticatedAction, checkObs: ObraSocialFilterAction) extends AbstractController(cc){


  def validar = (authAction andThen checkObs) { implicit request =>

    val ventaRepo = new VentaRepository

    val venta = jsonMapper.fromJson[Venta](request.rootNode.toString)
    val estadoNuevo = venta.validar(request.user)

    val futureV = ventaRepo.modificarVenta(venta, estadoNuevo)
    Await.result(futureV, Duration.Inf)
    Ok("validado")

  }


  def all = getAuthAction { implicit request =>
    val futureVentas = valiRepo.all(request.user)
    val ventas = Await.result(futureVentas, Duration.Inf)
    val json = jsonMapper.toJson(ventas)
    Ok(json)
  }

  def ventasAValidar = getAuthAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val futureVentas = valiRepo.ventasAValidar(request.user)
    val ventas = Await.result(futureVentas, Duration.Inf)
    val json = jsonMapper.toJson(ventas)
    Ok(json)
  }
}
