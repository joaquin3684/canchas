package controllers

import javax.inject.Inject

import actions.{AuthenticatedAction, GetAuthenticatedAction, JsonMapperAction, ObraSocialFilterAction}
import models._
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.{RecuperacionVentaRepository, UsuarioRepository}
import services.JsonMapper
import com.github.t3hnar.bcrypt._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class RecuperarVentaController @Inject()(cc: ControllerComponents, val jsonMapper: JsonMapper, authAction: AuthenticatedAction, getAuthAction: GetAuthenticatedAction, checkObs: ObraSocialFilterAction) extends AbstractController(cc) with Estados with Perfiles{

  def ventasRecuperables = getAuthAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val future = RecuperacionVentaRepository.ventasRecuperablesCall(request.user)
    val ventasRec = Await.result(future, Duration.Inf)
    val v = ventasRec.map { x =>

      val a = jsonMapper.toJsonString(x._1)
      val node = jsonMapper.getJsonNode(a)
      val es = jsonMapper.toJsonString(x._2)
      jsonMapper.putElement(node, "estado", es)
      node
    }.distinct
    val ventas = jsonMapper.toJson(v)
    Ok(ventas)
  }

  def recuperar = (authAction andThen checkObs) { implicit request =>
    val idEstado = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "idEstado").toLong
    val future = RecuperacionVentaRepository.recuperarVenta(idEstado)
    Await.result(future, Duration.Inf)
    Ok("venta recuperada")
  }

  def rechazo = (authAction andThen checkObs){ implicit request =>
    val idEstado = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "idEstado").toLong
    val obs = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "observacion").toString
    val future = RecuperacionVentaRepository.rechazarVenta(idEstado, obs)
    Await.result(future, Duration.Inf)
    Ok("venta rechazada")
  }

  def ventasParaMarcarComoRecuperar = getAuthAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val future = RecuperacionVentaRepository.ventasParaQueSeRecuperen
    val ventasRec = Await.result(future, Duration.Inf)
    val v = ventasRec.map { x =>

      val a = jsonMapper.toJsonString(x._1)
      val node = jsonMapper.getJsonNode(a)
      val es = jsonMapper.toJsonString(x._2)
      jsonMapper.putElement(node, "estado", es)
      node
    }.distinct
    val ventas = jsonMapper.toJson(v)
    Ok(ventas)
  }

  def marcarParaRecuperar = (authAction andThen checkObs){ implicit request =>
    val idEstado = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "idEstado").toLong
    val user = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "user").toString
    val idVenta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "idVenta").toLong

    val f = UsuarioRepository.getPerfilFromUser(user)
    val perfiles = Await.result(f, Duration.Inf)

    if(perfiles.exists(x => x == PROMOTORA || x == EXTERNO || x == VENDEDORA))
      {
        val future = RecuperacionVentaRepository.recuperarVentaSinCall(idEstado, user, idVenta)
        Await.result(future, Duration.Inf)
        Ok("venta recuperada")
      }
    else
    {
      val future = RecuperacionVentaRepository.marcarParaRecuperar(idEstado, user, idVenta)
      Await.result(future, Duration.Inf)
      Ok("venta enviada a call")
    }
  }

}
