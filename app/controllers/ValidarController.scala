package controllers

import javax.inject.Inject

import actions.{AuthenticatedAction, GetAuthenticatedAction, JsonMapperAction, ObraSocialFilterAction}
import models.{Validacion, Venta}
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.{ValidacionRepository, VentaRepository}
import services.JsonMapper

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

class ValidarController @Inject()(cc: ControllerComponents, val ventaRepo: VentaRepository, val valiRepo: ValidacionRepository, val jsonMapper: JsonMapper, jsonMapperAction: JsonMapperAction, authAction: AuthenticatedAction, val getAuthAction: GetAuthenticatedAction, checkObs: ObraSocialFilterAction) extends AbstractController(cc){


  def validar = (authAction andThen checkObs) { implicit request =>

    val venta = jsonMapper.fromJson[Validacion](request.rootNode.toString)
    val estadoNuevo = venta.validar(request.user)

    val futureV = valiRepo.validarVenta(venta, estadoNuevo)

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

  def modificarVenta = (authAction andThen checkObs) { implicit request =>
    val venta = jsonMapper.fromJson[Venta](request.rootNode.toString)
    val futureVenta = ventaRepo.modificarVenta(venta)
    val ventas = Await.result(futureVenta, Duration.Inf)

    Ok("modificado")
  }
}
