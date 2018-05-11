package controllers

import javax.inject.Inject

import actions.{AuthenticatedAction, GetAuthenticatedAction, JsonMapperAction, ObraSocialFilterAction}
import akka.http.scaladsl.model.DateTime
import models.{Estado, Visita}
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.{LogisticaRepository, VentaRepository}
import services.JsonMapper

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class LogisticaController @Inject()(cc: ControllerComponents, logisRepo: LogisticaRepository, ventaRepo: VentaRepository, val jsonMapper: JsonMapper, jsonMapperAction: JsonMapperAction, authAction: AuthenticatedAction, getAuthAction: GetAuthenticatedAction, checkObs: ObraSocialFilterAction) extends AbstractController(cc){


  def ventasSinVisita = getAuthAction {implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val futureVentas = logisRepo.ventasSinVisita
    val ven= Await.result(futureVentas, Duration.Inf)
    val ventas = jsonMapper.toJson(ven)
    Ok(ventas)
  }

  def altaVisita = (authAction andThen checkObs) { implicit request =>

    val rootNode = request.rootNode

    val dni = jsonMapper.getAndRemoveElement(rootNode, "dni").toInt

    jsonMapper.putElement(rootNode, "fecha", DateTime.now.toString())
    jsonMapper.putElement(rootNode, "idVenta", dni.toString)
    jsonMapper.putElement(rootNode, "estado", "Visita creada")
    jsonMapper.putElement(rootNode, "idUser", request.user)

    val visita = jsonMapper.fromJson[Visita](rootNode.toString)

    val futureVisita = logisRepo.create(visita)
    Await.result(futureVisita, Duration.Inf)

    Ok("visita creada")

  }

  def ventasAConfirmar = getAuthAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val futureVentas = logisRepo.ventasAConfirmar
    val ven= Await.result(futureVentas, Duration.Inf)
    val ventas = jsonMapper.toJson(ven)
    Ok(ventas)
  }

  def confirmarVisita = (authAction andThen checkObs) { implicit request =>

    val rootNode = request.rootNode

    val dni = rootNode.get("dni").asInt

    val estadoNuevo = Estado(request.user, dni, "Visita confirmada", DateTime.now)

    val futureEstado = ventaRepo.agregarEstado(estadoNuevo)
    Await.result(futureEstado, Duration.Inf)

    Ok("confirmada")
  }

  def getVisita(dni: Int) = getAuthAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val futureVisita = logisRepo.getVisita(dni)
    val visita = Await.result(futureVisita, Duration.Inf)
    val visitaJson = jsonMapper.toJson(visita)
    Ok(visitaJson)
  }

  def rechazar = (authAction andThen checkObs) { implicit request =>

    val rootNode = request.rootNode
    val dni = rootNode.get("dni").asInt

    val estadoNuevo = Estado(request.user, dni, "Rechazo por logistica", DateTime.now)

    val futureEstado = ventaRepo.agregarEstado(estadoNuevo)
    Await.result(futureEstado, Duration.Inf)

    Ok("rechazado")
  }

  def repactarVisita = authAction { implicit request =>

    val rootNode = request.rootNode

    val dni = rootNode.get("dni").asInt
    jsonMapper.putElement(rootNode, "fecha", DateTime.now.toString())
    jsonMapper.putElement(rootNode, "idVenta", dni.toString)
    jsonMapper.putElement(rootNode, "estado", "Visita creada")
    jsonMapper.putElement(rootNode, "idUser", request.user)

    val visita = jsonMapper.fromJson[Visita](rootNode.toString)

    val futureVisita = logisRepo.repactar(visita)
    Await.result(futureVisita, Duration.Inf)

    Ok("visita creada")

  }

  def getVisitas(dni: Int) = getAuthAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val futureVisitas = logisRepo.getVisitas(dni)
    val visitas = Await.result(futureVisitas, Duration.Inf)
    val visitasJson = jsonMapper.toJson(visitas)
    Ok(visitasJson)
  }

  def all = getAuthAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val futureVentas = logisRepo.all(request.user)
    val ven= Await.result(futureVentas, Duration.Inf)
    val ventas = jsonMapper.toJson(ven)
    Ok(ventas)
  }
}
