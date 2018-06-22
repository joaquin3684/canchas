package controllers

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
    Ok("anda")
  }

  def completarVenta = (authAction andThen checkObs) { implicit request =>

    val dni = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "dni").toInt
    val empresa = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "empresa")
    val cuit = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "cuit").toInt
    val tresPorciento = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "tresPorciento").toDouble

    val future = AdministracionVentaRepository.completarVenta(dni, empresa, cuit, tresPorciento)
    Await.result(future, Duration.Inf)
    Ok("venta completada")
  }

  def presentarVentas = authAction { implicit request =>
    val fechaString = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaPresentacion")
    val fechaPresentacion = DateTime.fromIsoDateTimeString(fechaString).get
    val dnis = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "dnis")
    val documentos = jsonMapper.fromJson[Seq[Int]](dnis)
    val future = AdministracionVentaRepository.presentarVentas(documentos, fechaPresentacion, request.user)
    Await.result(future, Duration.Inf)
    Ok("ventas presentadas")
  }

  def analizarPresentacion = (authAction andThen checkObs) { implicit request =>
    val dni = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "dni").toInt
    val estado = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "estado")
    val observacion = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "observacion")
    val fecha = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaPresentacion")


    val estadoNuevo = estado match {
      case "pagada" => Estado(request.user, dni, PAGADA, DateTime.now)
      case "rechazada" => Estado(request.user, dni, RECHAZO_ADMINISTRACION, DateTime.now, false, Some(observacion))
      case "pendiente auditoria" => Estado(request.user, dni, PRESENTADA, DateTime.fromIsoDateTimeString(fecha).get)
    }

    val future = AdministracionVentaRepository.analizarPresentacion(estadoNuevo)
    Await.result(future, Duration.Inf)

    Ok("venta analizada")
  }

  def digitalizarArchivos = (authAction andThen checkObs) { implicit request =>
    Ok("anda")
  }
}
