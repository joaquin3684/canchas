package controllers

import java.nio.file.Paths
import javax.inject.Inject

import actions.{AuthenticatedAction, GetAuthenticatedAction, JsonMapperAction, ObraSocialFilterAction}
import akka.http.scaladsl.model.DateTime
import models.{Auditoria, Estado, Estados}
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.AuditoriaRepository
import services.JsonMapper

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class AuditoriaController @Inject()(cc: ControllerComponents, checkObs: ObraSocialFilterAction, val jsonMapper: JsonMapper, jsonMapperAction: JsonMapperAction, val authAction: AuthenticatedAction, val getAuthAction: GetAuthenticatedAction) extends AbstractController(cc) with Estados{

  def all = getAuthAction {implicit request =>
    val futureVentas = AuditoriaRepository.all(request.user)
    val ventas = Await.result(futureVentas, Duration.Inf)
    val ventasJson = jsonMapper.toJson(ventas)
    Ok(ventasJson)
  }

  def ventasParaAuditar = getAuthAction { implicit request =>
    implicit val obs : Seq[String] = request.obrasSociales
    val futureVentas = AuditoriaRepository.ventasParaAuditar
    val ventas = Await.result(futureVentas, Duration.Inf)
    val ven = ventas.map(_._1).distinct
    val v = ven.map{ x =>
      val js = jsonMapper.toJsonString(x)
      val vNode = jsonMapper.getJsonNode(js)

      val perfiles = ventas.filter(_._1.id == x.id).map(_._2)
      val pjs = jsonMapper.toJsonString(perfiles)
      val pNode = jsonMapper.getJsonNode(pjs)

      jsonMapper.addNode("perfiles", pNode, vNode)

      vNode
    }
    val ventasJson = jsonMapper.toJson(v)
    Ok(ventasJson)
  }

  def upload = (authAction andThen checkObs) { implicit request =>


    val cantAudios = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "cantidadAudios").toInt
    val estado = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "estado").toString
    val nombre = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "nombre").toString
    val idVenta = request.rootNode.get("idVenta").asLong()
    val observacion = request.rootNode.get("observacion").asText()





    val es =  estado match {
      case "ok" => (Estado(request.user, idVenta, AUDITORIA_APROBADA, DateTime.now), "OK")
      case "rechazo" =>    {
        val recuperable = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "recuperable").toBoolean
        if(recuperable)
          (Estado(request.user, idVenta, RECHAZO_AUDITORIA, DateTime.now, recuperable, Some(observacion)), "RP" )
        else
          (Estado(request.user, idVenta, RECHAZO_AUDITORIA, DateTime.now, recuperable, Some(observacion)), "RT")
      }
      case "observado" => (Estado(request.user, idVenta, AUDITORIA_OBSERVADA, DateTime.now, false, Some(observacion)), "OB")
    }


    (1 to cantAudios).foreach { x =>

      val ruta = "http://gestionarturnos.com/ventas/auditorias/" + idVenta + "/" + nombre + "-AM-"+ es._2 +"-" + x +".mp3"
      jsonMapper.putElement(request.rootNode, "audio"+x, ruta)
    }

    if(estado != "rechazo") jsonMapper.removeElement(request.rootNode, "recuperable")
    val audi = jsonMapper.fromJson[Auditoria](request.rootNode.toString)
    val futureVenta = AuditoriaRepository.auditar(audi, es._1)
    Await.result(futureVenta, Duration.Inf)

    Ok("guardado")
  }
}
