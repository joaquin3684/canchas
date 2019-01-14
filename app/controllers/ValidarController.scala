package controllers

import javax.inject.Inject

import actions.{AuthenticatedAction, GetAuthenticatedAction, JsonMapperAction, ObraSocialFilterAction}
import akka.http.scaladsl.model.DateTime
import models.{DatosEmpresa, Estados, Validacion, Venta}
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.{ValidacionRepository, VentaRepository}
import services.JsonMapper

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

class ValidarController @Inject()(cc: ControllerComponents, val jsonMapper: JsonMapper, jsonMapperAction: JsonMapperAction, authAction: AuthenticatedAction, val getAuthAction: GetAuthenticatedAction, checkObs: ObraSocialFilterAction) extends AbstractController(cc) with Estados{


  def validar = (authAction andThen checkObs) { implicit request =>
    val datos = jsonMapper.getAndRemoveElement(request.rootNode, "datosEmpresa")
    val capitas = jsonMapper.getAndRemoveElement(request.rootNode, "capitas").toInt
    val d = jsonMapper.fromJson[DatosEmpresa](datos)
    val venta = jsonMapper.fromJson[Validacion](request.rootNode.toString)
    val estadoNuevo = venta.validar(request.user)


    val futureV = ValidacionRepository.validarVenta(venta, estadoNuevo, d, capitas)

    Await.result(futureV, Duration.Inf)
    Ok("validado")

  }

  def all = getAuthAction { implicit request =>
    val futureVentas = ValidacionRepository.all(request.user)
    val ventas = Await.result(futureVentas, Duration.Inf)
    val json = jsonMapper.toJson(ventas)
    Ok(json)
  }

  def ventasParaModificar = getAuthAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales

    val futureVentas = ValidacionRepository.ventasModificables
    val ventas = Await.result(futureVentas, Duration.Inf)
    val ven = ventas.map(_._1).distinct
    val v = ven.map {x =>
      val sv = jsonMapper.toJsonString(x)
      val vNode = jsonMapper.getJsonNode(sv)
      val e1 = ventas.filter(v => v._1.id == x.id && v._2.estado == CREADO).head
      val eFinal = ventas.filter(v => v._1.id == x.id).sortWith(_._2.id > _._2.id).head

      jsonMapper.putElement(vNode, "user", e1._2.user)
      jsonMapper.putElement(vNode, "fechaCreacion", e1._2.fecha.toIsoDateTimeString())
      jsonMapper.putElement(vNode, "ultimoEstado", eFinal._2.estado)
    }
    val json = jsonMapper.toJson(v)

    Ok(json)
  }

  def borrarVenta = authAction { implicit  request =>

    val idVenta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "id").toLong
    val future = ValidacionRepository.borrarVenta(idVenta)
     Await.result(future, Duration.Inf)
    Ok("borrado")
  }

  def ventasAValidar = getAuthAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val futureVentas = ValidacionRepository.ventasAValidar(request.user)
    val ventas = Await.result(futureVentas, Duration.Inf)
    val v = ventas.map{ x =>
      val sV = jsonMapper.toJsonString(x._1)
      val vNode = jsonMapper.getJsonNode(sV)
      val ejs = jsonMapper.toJsonString(x._2)
      jsonMapper.putElement(vNode, "fechaCreacion", x._2.toIsoDateTimeString)
      jsonMapper.putElement(vNode, "user", x._3)
      jsonMapper.putElement(vNode, "sector", x._4)


      vNode
    }

    val json = jsonMapper.toJson(v)
    Ok(json)
  }

  def modificarVenta(idVenta: Long) = (authAction andThen checkObs) { implicit request =>
    val user = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "user")
    val f = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaCreacion")

    jsonMapper.removeElement(request.rootNode, "idVenta")

    val fechaCreacion = DateTime.fromIsoDateTimeString(f).get
    val venta = jsonMapper.fromJson[Venta](request.rootNode.toString)
    val futureVenta = VentaRepository.modificarVenta(venta, idVenta, user, fechaCreacion)
    val ventas = Await.result(futureVenta, Duration.Inf)

    Ok("modificado")
  }
}
