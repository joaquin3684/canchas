package controllers

import javax.inject.Inject

import actions.{AuthenticatedAction, GetAuthenticatedAction, JsonMapperAction, ObraSocialFilterAction}
import akka.http.scaladsl.model.DateTime
import models.{Estado, Venta, Visita}
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.{LogisticaRepository, VentaRepository}
import services.JsonMapper

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class LogisticaController @Inject()(cc: ControllerComponents, val logisRepo: LogisticaRepository, val ventaRepo: VentaRepository, val jsonMapper: JsonMapper, authAction: AuthenticatedAction, getAuthAction: GetAuthenticatedAction, checkObs: ObraSocialFilterAction) extends AbstractController(cc){


  def ventasSinVisita = getAuthAction {implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val futureVentas = logisRepo.ventasSinVisita
    val ven= Await.result(futureVentas, Duration.Inf)
    val ventas = jsonMapper.toJson(ven)
    Ok(ventas)
  }

  def altaVisita = (authAction andThen checkObs) { implicit request =>

    val rootNode = request.rootNode


    jsonMapper.putElement(rootNode, "estado", "Visita creada")
    jsonMapper.putElement(rootNode, "user", request.user)

    val visita = jsonMapper.fromJson[Visita](rootNode.toString)

    val futureVisita = logisRepo.create(visita)
    Await.result(futureVisita, Duration.Inf)

    Ok("visita creada")

  }


  def ventasATrabajar = getAuthAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val futureVentas = logisRepo.ventasAConfirmar
    val ven= Await.result(futureVentas, Duration.Inf)
    val root = jsonMapper.mapper.createObjectNode()

    val venta = ven.map { x =>
      val obj = jsonMapper.mapper.createObjectNode()
      jsonMapper.addNode("venta", jsonMapper.getJsonNode(jsonMapper.toJsonString(x._1)), obj)
      jsonMapper.addNode("estado", jsonMapper.getJsonNode(jsonMapper.toJsonString(x._2)), obj)
      obj
    }
    Ok(jsonMapper.toJson(venta))
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
    val rootNode = jsonMapper.mapper.createObjectNode
    rootNode.put("dni", visita.dni)
    rootNode.put("direccion", visita.direccion)
    rootNode.put("entreCalles", visita.entreCalles)
    rootNode.put("lugar", visita.lugar)
    rootNode.put("localidad", visita.localidad)
    rootNode.put("observacion", visita.observacion.get)
    rootNode.put("fecha", visita.fecha.toIsoDateTimeString)

    val visitaJson = jsonMapper.toJson(rootNode)

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

    jsonMapper.putElement(rootNode, "estado", "Visita repactada")
    jsonMapper.putElement(rootNode, "user", request.user)

    val visita = jsonMapper.fromJson[Visita](rootNode.toString)

    val futureVisita = logisRepo.repactar(visita)
    Await.result(futureVisita, Duration.Inf)

    Ok("visita repactada")

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
    val ventasConVisitas = Await.result(futureVentas, Duration.Inf)
    val ventass = ventasConVisitas.map(_._1).distinct
    val visitas = ventasConVisitas.map(_._2)
    val a = ventass.map{x =>
        val j = jsonMapper.toJsonString(x)
        val vNode = jsonMapper.getJsonNode(j)
        val h = jsonMapper.toJsonString(visitas.filter(_.dni == x.dni))
        val visNode = jsonMapper.getJsonNode(h)
        jsonMapper.addNode("visitas", visNode, vNode)
       vNode
    }

    val ventas = jsonMapper.toJson(a)
    Ok(ventas)
  }
}
