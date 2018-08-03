package controllers

import javax.inject.Inject

import actions.{AuthenticatedAction, GetAuthenticatedAction, JsonMapperAction, ObraSocialFilterAction}
import akka.http.scaladsl.model.DateTime
import models.{Estado, Estados, Venta, Visita}
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.{LogisticaRepository, VentaRepository}
import services.JsonMapper

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class LogisticaController @Inject()(cc: ControllerComponents, val jsonMapper: JsonMapper, authAction: AuthenticatedAction, getAuthAction: GetAuthenticatedAction, checkObs: ObraSocialFilterAction) extends AbstractController(cc) with Estados{


  def altaVisita = (authAction andThen checkObs) { implicit request =>

    val rootNode = request.rootNode


    jsonMapper.putElement(rootNode, "estado", VISITA_CREADA)

    val visita = jsonMapper.fromJson[Visita](rootNode.toString)
    val es = Estado(request.user, visita.idVenta, VISITA_CREADA, DateTime.now)

    val futureVisita = LogisticaRepository.create(visita, es)
    Await.result(futureVisita, Duration.Inf)

    Ok("visita creada")

  }

  def ventasSinVisita = getAuthAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val futureVentas = LogisticaRepository.ventasSinVisita
    val ven = Await.result(futureVentas, Duration.Inf)
    Ok(jsonMapper.toJson(ven))
  }

  def ventasATrabajar = getAuthAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val futureVentas = LogisticaRepository.ventasAConfirmar
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

    val idVenta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "idVenta").toLong
    val idVisita = rootNode.get("idVisita").asLong
    val estadoNuevo = Estado(request.user, idVenta, VISITA_CONFIRMADA, DateTime.now)


    val futureEstado = LogisticaRepository.confirmarVisita(idVisita, estadoNuevo)
    Await.result(futureEstado, Duration.Inf)

    Ok("confirmada")
  }


  def enviarACall = (authAction andThen checkObs) { implicit request =>

    val rootNode = request.rootNode

    val idVenta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "idVenta").toLong
    val idVisita = rootNode.get("idVisita").asLong

    val futureEstado = LogisticaRepository.enviarACall(idVisita, idVenta)
    Await.result(futureEstado, Duration.Inf)

    Ok("enviadoACall")
  }

  def getVisita(idVenta: Long) = getAuthAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val futureVisita = LogisticaRepository.getVisita(idVenta)
    val visita = Await.result(futureVisita, Duration.Inf)


    val visitaJson = jsonMapper.toJson(visita)

    Ok(visitaJson)
  }

  def rechazar = (authAction andThen checkObs) { implicit request =>

    val rootNode = request.rootNode
    val idVenta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "idVenta").toLong
    val observacion = if(rootNode.get("observacion").toString.isEmpty) None else Some(jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(rootNode, "observacion").toString)
    val recuperable = rootNode.get("recuperable").asBoolean
    val estadoNuevo = Estado(request.user, idVenta, RECHAZO_LOGISTICA, DateTime.now, recuperable, observacion)
    val futureEstado = VentaRepository.agregarEstado(estadoNuevo)
    Await.result(futureEstado, Duration.Inf)

    Ok("rechazado")
  }


  def asignarUsuario = (authAction andThen checkObs) {implicit request =>
    val idVisita = request.rootNode.get("idVisita").asInt
    val usuario = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "user").toString
    val future = LogisticaRepository.asignarUsuario(usuario, idVisita)
    Await.result(future, Duration.Inf)
    Ok("asignado")
  }

  def repactarVisita = authAction { implicit request =>

    val rootNode = request.rootNode

    jsonMapper.putElement(rootNode, "estado", VISITA_REPACTADA)

    val visita = jsonMapper.fromJson[Visita](rootNode.toString)
    val es = Estado(request.user, visita.idVenta, VISITA_REPACTADA, DateTime.now)

    val futureVisita = LogisticaRepository.repactar(visita, es)
    Await.result(futureVisita, Duration.Inf)

    Ok("visita repactada")

  }

  /*def getVisitas(dni: Int) = getAuthAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val futureVisitas = LogisticaRepository.getVisitas(dni)
    val visitas = Await.result(futureVisitas, Duration.Inf)
    val visitasJson = jsonMapper.toJson(visitas)
    Ok(visitasJson)
  }*/

  def all = getAuthAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val futureVentas = LogisticaRepository.all(request.user)
    val ventasConVisitas = Await.result(futureVentas, Duration.Inf)
    val ventass = ventasConVisitas.map(_._1).distinct
    val visitas = ventasConVisitas.map(_._2)
    val a = ventass.map{x =>
        val j = jsonMapper.toJsonString(x)
        val vNode = jsonMapper.getJsonNode(j)
        val h = jsonMapper.toJsonString(visitas.filter(_.idVenta == x.id))
        val visNode = jsonMapper.getJsonNode(h)
        jsonMapper.addNode("visitas", visNode, vNode)
       vNode
    }

    val ventas = jsonMapper.toJson(a)
    Ok(ventas)
  }
}
