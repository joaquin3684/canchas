package controllers

import java.nio.file.Paths
import javax.inject.Inject

import actions.{AuthenticatedAction, GetAuthenticatedAction, JsonMapperAction}
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

  def upload=  Action(parse.multipartFormData) { implicit request =>

    //val a = request.body.dataParts.flatMap()
    request.body.file("picture").map { picture =>

      // only get the last part of the filename
      // otherwise someone can send a path like ../../home/foo/bar.txt to write to other files on the system
      val reg_ex = """.*\.(\w+)""".r

      val filename = Paths.get(picture.filename).getFileName

      picture.ref.moveTo(Paths.get("public/images/tmp.png"), replace = true)
      Ok("File uploaded")
    }.getOrElse {
      Ok("esto no anda")
    }
  }
}
