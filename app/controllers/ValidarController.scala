package controllers

import javax.inject.Inject

import actions.{AuthenticatedAction, GetAuthenticatedAction, JsonMapperAction}
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.{ValidacionRepository, VentaRepository}
import services.JsonMapper

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ValidarController @Inject()(cc: ControllerComponents, val valiRepo: ValidacionRepository, val jsonMapper: JsonMapper, jsonMapperAction: JsonMapperAction, val authAction: AuthenticatedAction, val getAuthAction: GetAuthenticatedAction) extends AbstractController(cc){


  def validar = authAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val dni = jsonMapper.getAndRemoveElement(request.rootNode, "dni").toInt
    val optionVenta = valiRepo.checkObraSocial(dni)

    if(optionVenta.nonEmpty) {
      val ventaRepo = new VentaRepository
      val venta = optionVenta.get
      val codem = Option(request.rootNode.get("codem").asBoolean)
      val supper = Option(request.rootNode.get("supper").asBoolean)
      val afip = Option(request.rootNode.get("afip").asBoolean)
      val motivoCodem = Option(request.rootNode.get("motivoCodem").asText)
      val motivoSupper = Option(request.rootNode.get("motivoSupper").asText)
      val motivoAfip = Option(request.rootNode.get("motivoAfip").asText)
      val datos = (codem, supper, afip, motivoCodem, motivoSupper, motivoAfip)

      val (ventaModificada, estadoNuevo) = venta.validar(datos, request.user)

      ventaRepo.modificarVenta(ventaModificada, estadoNuevo)

      Ok("validado")
    } else throw new RuntimeException("obra social erronea")

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
    Ok(json)  }
}
