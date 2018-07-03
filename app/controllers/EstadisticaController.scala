package controllers

import javax.inject.Inject

import actions.{AuthenticatedAction, GetAuthenticatedAction, JsonMapperAction, ObraSocialFilterAction}
import akka.http.scaladsl.model.DateTime
import models._
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.{EstadisticaRepository, RecuperacionVentaRepository, UsuarioRepository}
import services.JsonMapper
import com.github.t3hnar.bcrypt._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class EstadisticaController @Inject()(cc: ControllerComponents, val jsonMapper: JsonMapper, authAction: AuthenticatedAction, getAuthAction: GetAuthenticatedAction, checkObs: ObraSocialFilterAction) extends AbstractController(cc) with Estados{

  def general = authAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val fdesde = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaDesde")
    val fhasta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaHasta")
    val fechaDesde = DateTime.fromIsoDateTimeString(fdesde).get
    val fechaHasta = DateTime.fromIsoDateTimeString(fhasta).get


    val future = EstadisticaRepository.estadisticaGeneral(fechaDesde, fechaHasta)
    val ventasEst = Await.result(future, Duration.Inf)
    val ventasDistintas = ventasEst.map(_._1).distinct
    val v = ventasDistintas.map { x =>

      val a = jsonMapper.toJsonString(x)
      val node = jsonMapper.getJsonNode(a)

      val es = ventasEst.filter(_._2.dni == x.dni).map(_._2).distinct
      val esJ = jsonMapper.toJsonString(es)
      val esNode = jsonMapper.getJsonNode(esJ)
      jsonMapper.addNode("estados", esNode, node)

      val vali = ventasEst.filter(v => v._4.isDefined && v._4.get.dni == x.dni).map(_._4.get).distinct.headOption
      val valiJ = jsonMapper.toJsonString(vali)
      val valiNode = jsonMapper.getJsonNode(valiJ)
      jsonMapper.addNode("validacion", valiNode, node)

      val vis = ventasEst.filter(v => v._3.isDefined && v._3.get.dni == x.dni).map(_._3.get).distinct
      val visJ = jsonMapper.toJsonString(vis)
      val visNode = jsonMapper.getJsonNode(visJ)
      jsonMapper.addNode("visitas", visNode, node)

      val audi = ventasEst.filter(v => v._5.isDefined && v._5.get.dni == x.dni).map(_._5.get).distinct.headOption
      val audiJ = jsonMapper.toJsonString(audi)
      val audiNode = jsonMapper.getJsonNode(audiJ)
      jsonMapper.addNode("auditoria", audiNode, node)

      val user = ventasEst.map(_._6).distinct.head
      val perfil = ventasEst.filter(x => x._6 == user).map(_._7).distinct.head
      val userJ = jsonMapper.toJsonString(user)
      val userNode = jsonMapper.getJsonNode(userJ)
      jsonMapper.putElement(userNode, "perfil", perfil)
      jsonMapper.addNode("usuario", userNode, node)


      node
    }
    val ventas = jsonMapper.toJson(v)
    Ok(ventas)
  }

}
