package controllers

import java.nio.file.Paths
import javax.inject.Inject

import actions.{AuthenticatedAction, GetAuthenticatedAction, JsonMapperAction}
import akka.http.scaladsl.model.DateTime
import models.{Auditoria, Estado, Estados}
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.AuditoriaRepository
import services.JsonMapper

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class AuditoriaController @Inject()(cc: ControllerComponents, val jsonMapper: JsonMapper, jsonMapperAction: JsonMapperAction, val authAction: AuthenticatedAction, val getAuthAction: GetAuthenticatedAction) extends AbstractController(cc) with Estados{

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
    val ventasJson = jsonMapper.toJson(ventas)
    Ok(ventasJson)
  }

  def upload = getAuthAction(parse.multipartFormData) { implicit request =>

    request.body.file("audio").map { picture =>

      val dni = request.body.dataParts.get("dni").get.head.toInt
      val observacion = if (request.body.dataParts.get("observacion").isDefined) Some(request.body.dataParts.get("observacion").get.head) else None
      val recuperacion = if (request.body.dataParts.get("recuperable").isDefined) request.body.dataParts.get("recuperable").get.head.toBoolean else false

      val estado = request.body.dataParts.get("estado").get.head match {
        case "ok" => Estado(request.user, dni, AUDITORIA_APROBADA, DateTime.now)
        case "rechazo" =>      Estado(request.user, dni, RECHAZO_AUDITORIA, DateTime.now, recuperacion, observacion)
        case "observado" => Estado(request.user, dni, AUDITORIA_OBSERVADA, DateTime.now, false, observacion)
      }

      val empresa = if(request.body.dataParts.get("empresa").isDefined) Some(request.body.dataParts.get("empresa").get.head) else None
      val direccion = if(request.body.dataParts.get("direccion").isDefined) Some(request.body.dataParts.get("direccion").get.head) else None
      val localidad = if(request.body.dataParts.get("localidad").isDefined) Some(request.body.dataParts.get("localidad").get.head) else None
      val cantidadEmpleados = if(request.body.dataParts.get("cantidadEmpleados").isDefined) Some(request.body.dataParts.get("cantidadEmpleados").get.head) else None
      val horaEntrada = if(request.body.dataParts.get("horaEntrada").isDefined) Some(request.body.dataParts.get("horaEntrada").get.head) else None
      val horaSalida = if(request.body.dataParts.get("horaSalida").isDefined) Some(request.body.dataParts.get("horaSalida").get.head) else None


      val rutaAudio = "public/images/"+ dni + ".mp3"
      val auditoria = Auditoria(dni, rutaAudio, observacion, empresa, direccion, localidad, cantidadEmpleados, horaEntrada, horaSalida)

      val futureVenta = AuditoriaRepository.auditar(auditoria, estado)
      Await.result(futureVenta, Duration.Inf)

      val filename = Paths.get(picture.filename).getFileName

      picture.ref.moveTo(Paths.get(rutaAudio), replace = true)
      Ok("File uploaded")
    }.getOrElse {
      Ok("esto no anda")
    }
  }
}
