package controllers

import javax.inject.Inject

import actions.{AuthenticatedAction, GetAuthenticatedAction, JsonMapperAction}
import akka.http.scaladsl.model.DateTime
import models.{Estado, Estados, Venta}
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.VentaRepository
import services.JsonMapper
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

class VentaController @Inject()(cc: ControllerComponents, val jsonMapper: JsonMapper, val authAction: AuthenticatedAction, val getAuthAction: GetAuthenticatedAction) extends AbstractController(cc) with Estados{

  def create = authAction { implicit request =>

    val userName = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "user")
    val f = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaCreacion")
    val fechaCreacion = DateTime.fromIsoDateTimeString(f).get
    val ventasJson = request.rootNode.toString
    val venta = jsonMapper.fromJson[Venta](ventasJson)
    if(request.obrasSociales.contains(venta.idObraSocial)) {
      val futureVenta = VentaRepository.create(venta, userName, fechaCreacion)

      futureVenta onComplete {
        case Success(ven) => {
          val futEs = VentaRepository.agregarEstado(Estado(userName, ven.id, CREADO, fechaCreacion))
          Await.result(futEs, Duration.Inf)
      }
        case Failure(t) => throw new RuntimeException("hubo un problema al cargar la venta")
      }
      Ok("creado")
    } else throw new RuntimeException("obra social erronea")
  }

  def all = getAuthAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val futureVentas = VentaRepository.all(request.user)
    val ventas = Await.result(futureVentas, Duration.Inf)
    val v = ventas.map {x =>
      val sv = jsonMapper.toJsonString(x._1)
      val vNode = jsonMapper.getJsonNode(sv)
      jsonMapper.putElement(vNode, "user", x._2.user)
      jsonMapper.putElement(vNode, "fechaCreacion", x._2.fecha.toIsoDateTimeString())
    }
    val json = jsonMapper.toJson(v)
    Ok(json)
  }

  def checkDniExistence = authAction { implicit request =>
    val dni = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "dni").toInt
    val future = VentaRepository.checkDni(dni)
    val v = Await.result(future, Duration.Inf)
    if(!v.isEmpty)
    {
      val venta = v.map(_._1).sortBy(- _.id).head
      val estado = v.map(_._2).filter(_.idVenta == venta.id).sortBy(- _.id).head

      val js = jsonMapper.toJsonString(venta)
      val vNode = jsonMapper.getJsonNode(js)
      val esjs = jsonMapper.toJsonString(estado)
      val esNode = jsonMapper.getJsonNode(esjs)
      jsonMapper.addNode("estado", esNode, vNode)
      Ok(jsonMapper.toJson(vNode.toString))
    } else {

      Ok("la venta no esta registrada")
    }
  }
}
