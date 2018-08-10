package controllers

import java.nio.file.Paths
import javax.inject.Inject

import actions.{AuthenticatedAction, GetAuthenticatedAction, JsonMapperAction, ObraSocialFilterAction}
import akka.http.scaladsl.model.DateTime
import models.{Estado, Estados, Venta, Visita}
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.{AdministracionVentaRepository, LogisticaRepository, VentaRepository}
import services.JsonMapper

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class AdministracionVentaController @Inject()(cc: ControllerComponents, val jsonMapper: JsonMapper, authAction: AuthenticatedAction, getAuthAction: GetAuthenticatedAction, checkObs: ObraSocialFilterAction) extends AbstractController(cc) with Estados{

  def ventasIncompletas = getAuthAction { implicit request =>
    implicit val obs : Seq[String] = request.obrasSociales
    val future = AdministracionVentaRepository.ventasIncompletas
    val ventasInc = Await.result(future, Duration.Inf)

    val ventasIncompletas = jsonMapper.toJson(ventasInc)
    Ok(ventasIncompletas)
  }

  def ventasPresentables = getAuthAction { implicit request =>
    implicit val obs : Seq[String] = request.obrasSociales
    val future = AdministracionVentaRepository.ventasPresentables
    val ventasPres = Await.result(future, Duration.Inf)
    val v = ventasPres.map { x =>

      val a = jsonMapper.toJsonString(x._1)
      val node = jsonMapper.getJsonNode(a)
      jsonMapper.putElement(node, "perfil", x._4)
      jsonMapper.putElement(node, "capitas", x._2.toString)
      jsonMapper.putElement(node, "nombreUsuario", x._3)
      jsonMapper.putElement(node, "fechaCreacion", x._5.toIsoDateTimeString)
      node

    }.distinct
    val ventas = jsonMapper.toJson(v)
    Ok(ventas)
  }

  def ventasPresentadas = getAuthAction { implicit request =>
    implicit val obs : Seq[String] = request.obrasSociales
    val future = AdministracionVentaRepository.ventasPresentadas
    val ventasPres = Await.result(future, Duration.Inf)
    val v = ventasPres.map{ x =>
      val a = jsonMapper.toJsonString(x._1)
      val node = jsonMapper.getJsonNode(a)
      jsonMapper.putElement(node, "fecha", x._2.toString())

      node
    }

    val ventas = jsonMapper.toJson(v)
    Ok(ventas)
  }

  def ventasPagadas = getAuthAction { implicit request =>
    implicit val obs : Seq[String] = request.obrasSociales
    val future = AdministracionVentaRepository.ventasPagadas
    val ventasPag = Await.result(future, Duration.Inf)
    val v = ventasPag.map { x =>
      val a = jsonMapper.toJsonString(x._1)
      val node = jsonMapper.getJsonNode(a)
      jsonMapper.putElement(node, "fechaPresentacion", x._2.toIsoDateTimeString())
      node
    }
    val ventas = jsonMapper.toJson(v)
    Ok(ventas)
  }

  def completarVenta = (authAction andThen checkObs) { implicit request =>

    val idVenta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "idVenta").toLong
    val empresa = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "empresa")
    val cuit = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "cuit")
    val tresPorciento = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "tresPorciento").toDouble

    val future = AdministracionVentaRepository.completarVenta(idVenta, empresa, cuit, tresPorciento)
    Await.result(future, Duration.Inf)
    Ok("venta completada")
  }

  def presentarVentas = authAction { implicit request =>
    val fechaString = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaPresentacion")
    val fechaPresentacion = DateTime.fromIsoDateTimeString(fechaString).get
    val ids = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "ids")
    val documentos = jsonMapper.fromJson[Seq[Long]](ids)
    val future = AdministracionVentaRepository.presentarVentas(documentos, fechaPresentacion, request.user)
    Await.result(future, Duration.Inf)
    Ok("ventas presentadas")
  }

  def analizarPresentacion = (authAction andThen checkObs) { implicit request =>
    val idVenta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "idVenta").toLong
    val estado = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "estado")
    val observacion = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "observacion")


    val estadoNuevo = estado match {
      case "PAGADA" => Estado(request.user, idVenta, PAGADA, DateTime.now)
      case "RECHAZADA" => Estado(request.user, idVenta, RECHAZO_PRESENTACION, DateTime.now, false, Some(observacion))
      case "PENDIENTE AUDITORIA" => Estado(request.user, idVenta, "estado auxiliar", DateTime.now)
    }

    val future = AdministracionVentaRepository.analizarPresentacion(estadoNuevo)
    Await.result(future, Duration.Inf)

    Ok("venta analizada")
  }

  def rechazar = (authAction andThen checkObs) { implicit request =>
    val idVenta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "idVenta").toLong
    val tipoRechazo = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "recuperable").toBoolean
    val observacion = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "observacion")
    val estado = Estado(request.user, idVenta, RECHAZO_ADMINISTRACION, DateTime.now, tipoRechazo, Some(observacion))
    val future = VentaRepository.agregarEstado(estado)
    Await.result(future, Duration.Inf)
    Ok("rechazado")
  }

  def ventasRechazables = getAuthAction { implicit request =>
    implicit val obs : Seq[String] = request.obrasSociales
    val future = AdministracionVentaRepository.ventasRechazables
    val ventasR = Await.result(future, Duration.Inf)

    val ventas = jsonMapper.toJson(ventasR)
    Ok(ventas)
  }

  def digitalizarArchivos = (authAction andThen checkObs) { implicit request =>

    val idVenta = request.rootNode.get("idVenta").asLong()
    val estado = Estado(request.user, idVenta, DIGITALIZADA, DateTime.now)
    val future = VentaRepository.agregarEstado(estado)
    Await.result(future, Duration.Inf)
    Ok("digitalizado")
  }
}
