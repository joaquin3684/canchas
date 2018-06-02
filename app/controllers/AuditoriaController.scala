package controllers

import java.nio.file.Paths
import javax.inject.Inject

import actions.{AuthenticatedAction, GetAuthenticatedAction, JsonMapperAction}
import akka.http.scaladsl.model.DateTime
import models.Estado
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.{AuditoriaRepository, VentaRepository}
import services.JsonMapper

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class AuditoriaController @Inject()(cc: ControllerComponents, val audiRepo: AuditoriaRepository, val ventaRepo: VentaRepository, val jsonMapper: JsonMapper, jsonMapperAction: JsonMapperAction, val authAction: AuthenticatedAction, val getAuthAction: GetAuthenticatedAction) extends AbstractController(cc){

  def all = getAuthAction {implicit request =>
    val futureVentas = audiRepo.all(request.user)
    val ventas = Await.result(futureVentas, Duration.Inf)
    val ventasJson = jsonMapper.toJson(ventas)
    Ok(ventasJson)
  }

  def ventasParaAuditar = getAuthAction { implicit request =>
    implicit val obs : Seq[String] = request.obrasSociales
    val futureVentas = audiRepo.ventasParaAuditar
    val ventas = Await.result(futureVentas, Duration.Inf)
    val ventasJson = jsonMapper.toJson(ventas)
    Ok(ventasJson)
  }

/*  def vectorToElem[A <: Any](a: Vector[A]) = a match {
    case Vector[Int] => "ha"
  }*/

  def upload = getAuthAction(parse.multipartFormData) { implicit request =>



    request.body.file("audio").map { picture =>


      val dni = request.body.dataParts.get("dni").get.head.toInt

      val estado = request.body.dataParts.get("estado").get.head match {
        case "ok" => Estado(request.user, dni, "Auditoria aprobada", DateTime.now)
        case "rechazo" => Estado(request.user, dni, "Auditoria rechazada", DateTime.now)
        case "observado" => Estado(request.user, dni, "Auditoria observada", DateTime.now)
      }

      val observacion = if (request.body.dataParts.get("observacion").isDefined) Some(request.body.dataParts.get("observacion").get.head) else None


      val ruta = "public/images/"+ 1 + ".mp3"
      val futureVenta = audiRepo.auditar(estado, dni, observacion, Some(ruta))
      Await.result(futureVenta, Duration.Inf)

      // only get the last part of the filename
      // otherwise someone can send a path like ../../home/foo/bar.txt to write to other files on the system
      val reg_ex = """.*\.(\w+)""".r

      val filename = Paths.get(picture.filename).getFileName

      picture.ref.moveTo(Paths.get(ruta), replace = true)
      Ok("File uploaded")
    }.getOrElse {
      Ok("esto no anda")
    }
  }
}
