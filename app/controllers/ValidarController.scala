package controllers

import javax.inject.Inject

import actions.{AuthenticatedAction, GetAuthenticatedAction, JsonMapperAction}
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.ValidacionRepository
import services.JsonMapper

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ValidarController @Inject()(cc: ControllerComponents, val valiRepo: ValidacionRepository, val jsonMapper: JsonMapper, jsonMapperAction: JsonMapperAction, val authAction: AuthenticatedAction, val getAuthAction: GetAuthenticatedAction) extends AbstractController(cc){


  def validar = authAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val dni = jsonMapper.getAndRemoveElement(request.rootNode, "dni").toInt
    val futureCheckObs = valiRepo.checkObraSocial(dni)
    val codem = Option(request.rootNode.get("codem").asBoolean)
    val supper = Option(request.rootNode.get("supper").asBoolean)
    val afip = Option(request.rootNode.get("afip").asBoolean)
    val motivoCodem = Option(request.rootNode.get("motivoCodem").asText)
    val motivoSupper = Option(request.rootNode.get("motivoSupper").asText)
    val motivoAfip = Option(request.rootNode.get("motivoAfip").asText)
    val datos = (codem, supper, afip, motivoCodem, motivoSupper, motivoAfip)

    val check = Await.result(futureCheckObs, Duration.Inf)
    if(check.nonEmpty) {
      val futureValidacion = valiRepo.validar(datos, dni, request.user)
      Await.result(futureValidacion, Duration.Inf)
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
