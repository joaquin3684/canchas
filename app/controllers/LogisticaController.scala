package controllers

import javax.inject.Inject

import actions.{AuthenticatedAction, GetAuthenticatedAction, JsonMapperAction}
import akka.http.scaladsl.model.DateTime
import models.{Estado, Visita}
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.{LogisticaRepository, VentaRepository}
import services.JsonMapper

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class LogisticaController @Inject()(cc: ControllerComponents, val logisRepo: LogisticaRepository, val ventaRepo: VentaRepository, val jsonMapper: JsonMapper, jsonMapperAction: JsonMapperAction, val authAction: AuthenticatedAction, val getAuthAction: GetAuthenticatedAction) extends AbstractController(cc){


  def ventasSinVisita = getAuthAction {implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val futureVentas = logisRepo.ventasSinVisita
    val ven= Await.result(futureVentas, Duration.Inf)
    val ventas = jsonMapper.toJson(ven)
    Ok(ventas)
  }

  def altaVisita = authAction { implicit request =>

    implicit val obs: Seq[String] = request.obrasSociales
    val rootNode = request.rootNode

    val dni = rootNode.get("dni").asInt

    val futureCheckObs = ventaRepo.checkObraSocial(dni)

    val optionVenta= Await.result(futureCheckObs, Duration.Inf)
    if(optionVenta.nonEmpty) {

      val lugar = rootNode.get("lugar").asText
      val direccion = rootNode.get("direccion").asText
      val entreCalles = rootNode.get("entreCalles").asText
      val localidad = rootNode.get("localidad").asText
      val observacion = rootNode.get("observacion").asText

      val visita = Visita(1, dni, request.user, lugar, direccion, entreCalles, localidad, observacion, DateTime.now, "Visita creada")

      val futureVisita = logisRepo.create(visita)
      Await.result(futureVisita, Duration.Inf)

      Ok("visita creada")

    } else throw new RuntimeException("no tiene permiso a esta obra social")
  }

  def ventasAConfirmar = getAuthAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val futureVentas = logisRepo.ventasAConfirmar
    val ven= Await.result(futureVentas, Duration.Inf)
    val ventas = jsonMapper.toJson(ven)
    Ok(ventas)
  }

  def confirmarVisita = authAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val rootNode = request.rootNode

    val idVenta = rootNode.get("idVenta").asInt

    val futureCheckObs = ventaRepo.checkObraSocial(idVenta)

    val optionVenta= Await.result(futureCheckObs, Duration.Inf)
    if(optionVenta.nonEmpty) {
      val estadoNuevo = Estado(request.user, idVenta, "Visita confirmada", DateTime.now)
      val futureEstado = ventaRepo.agregarEstado(estadoNuevo)
      Await.result(futureEstado, Duration.Inf)
      Ok("confirmada")
    } else throw new RuntimeException("obra social erronea")
  }

  def repactarVisita = authAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val rootNode = request.rootNode

    val dni = rootNode.get("dni").asInt

    val futureCheckObs = ventaRepo.checkObraSocial(dni)

    val optionVenta= Await.result(futureCheckObs, Duration.Inf)
    if(optionVenta.nonEmpty) {

      val lugar = rootNode.get("lugar").asText
      val direccion = rootNode.get("direccion").asText
      val entreCalles = rootNode.get("entreCalles").asText
      val localidad = rootNode.get("localidad").asText
      val observacion = rootNode.get("observacion").asText

      val visita = Visita(1, dni, request.user, lugar, direccion, entreCalles, localidad, observacion, DateTime.now, "Visita repactada")

      val futureVisita = logisRepo.repactar(visita)
      Await.result(futureVisita, Duration.Inf)

      Ok("visita creada")

    } else throw new RuntimeException("no tiene permiso a esta obra social")
  }

  def find(dni: Int) = getAuthAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val futureVentas = logisRepo.getVisitas(dni)
    val ven= Await.result(futureVentas, Duration.Inf)
    val ventas = jsonMapper.toJson(ven)
    Ok(ventas)
  }

  def all = getAuthAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val futureVentas = logisRepo.all(request.user)
    val ven= Await.result(futureVentas, Duration.Inf)
    val ventas = jsonMapper.toJson(ven)
    Ok(ventas)
  }
}
