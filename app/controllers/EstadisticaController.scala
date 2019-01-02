package controllers

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import actions.{AuthenticatedAction, GetAuthenticatedAction, JsonMapperAction, ObraSocialFilterAction}
import akka.http.scaladsl.model.DateTime
import models._
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.{EstadisticaRepository, RecuperacionVentaRepository, UsuarioRepository}
import services.JsonMapper
import com.github.t3hnar.bcrypt._
import java.text.NumberFormat.getCurrencyInstance
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class EstadisticaController @Inject()(cc: ControllerComponents, val jsonMapper: JsonMapper, authAction: AuthenticatedAction, getAuthAction: GetAuthenticatedAction, checkObs: ObraSocialFilterAction) extends AbstractController(cc) with Estados with Perfiles {

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

      val es = ventasEst.filter(_._2.idVenta == x.id).map(_._2).distinct
      val esJ = jsonMapper.toJsonString(es)
      val esNode = jsonMapper.getJsonNode(esJ)
      jsonMapper.addNode("estados", esNode, node)

      val vali = ventasEst.filter(v => v._4.isDefined && v._4.get.idVenta == x.id).map(_._4.get).distinct.headOption
      val valiJ = jsonMapper.toJsonString(vali)
      val valiNode = jsonMapper.getJsonNode(valiJ)
      jsonMapper.addNode("validacion", valiNode, node)

      val vis = ventasEst.filter(v => v._3.isDefined && v._3.get.idVenta == x.id).map(_._3.get).distinct
      val visJ = jsonMapper.toJsonString(vis)
      val visNode = jsonMapper.getJsonNode(visJ)
      jsonMapper.addNode("visitas", visNode, node)

      val audi = ventasEst.filter(v => v._5.isDefined && v._5.get.idVenta == x.id).map(_._5.get).distinct.headOption
      val audiJ = jsonMapper.toJsonString(audi)
      val audiNode = jsonMapper.getJsonNode(audiJ)
      jsonMapper.addNode("auditoria", audiNode, node)

      val user = ventasEst.filter(_._1 == x ).map(_._6).distinct.head
      val perfil = ventasEst.filter(x => x._6 == user && (x._7 == "OPERADOR VENTA" || x._7 == "EXTERNO" || x._7 == "VENDEDORA" || x._7 == "PROMOTORA" || x._7 == "ADMIN")).map(_._7).distinct.head
      val userJ = jsonMapper.toJsonString(user)
      val userNode = jsonMapper.getJsonNode(userJ)
      jsonMapper.putElement(userNode, "perfil", perfil)
      jsonMapper.addNode("usuario", userNode, node)


      node
    }
    val ventas = jsonMapper.toJson(v)
    Ok(ventas)
  }

  def estados = getAuthAction {implicit request =>
    val future = EstadisticaRepository.states
    val estados = Await.result(future, Duration.Inf)

    Ok(jsonMapper.toJson(estados))
  }

  def visitas = authAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val fdesde = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaDesde")
    val fhasta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaHasta")
    val fvdesde = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaDesdeVisita")
    val fvhasta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaHastaVisita")
    val fechaDesde = DateTime.fromIsoDateTimeString(fdesde).get
    val fechaHasta = DateTime.fromIsoDateTimeString(fhasta).get
    val fechaDesdeVisita = DateTime.fromIsoDateTimeString(fvdesde).get
    val fechaHastaVisita = DateTime.fromIsoDateTimeString(fvhasta).get

    val future = EstadisticaRepository.estadisticasVisitas(fechaDesde, fechaHasta, fechaDesdeVisita, fechaHastaVisita)
    val v = Await.result(future, Duration.Inf)

    val vis = v.map(_._1).distinct

    val venta = vis.map{ x =>
      val a = jsonMapper.toJsonString(x)
      val node = jsonMapper.getJsonNode(a)

      val e = v.filter(_._3.idVenta == x.idVenta).sortBy(- _._3.id).map(_._3.estado).headOption
      val ven = v.filter(_._2.id == x.idVenta).map(_._2).head
      jsonMapper.putElement(node, "estado", e.getOrElse(""))
      jsonMapper.putElement(node, "prestadora", ven.idObraSocial)
      jsonMapper.putElement(node, "base", ven.base.getOrElse(""))

      node

    }

    Ok(jsonMapper.toJson(venta))
  }

  def archivos = authAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val fdesde = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaDesde")
    val fhasta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaHasta")
    val fechaDesde = DateTime.fromIsoDateTimeString(fdesde).get
    val fechaHasta = DateTime.fromIsoDateTimeString(fhasta).get
    val future = EstadisticaRepository.archivos(fechaDesde, fechaHasta)
    val arch = Await.result(future, Duration.Inf)
    val ventas = arch.map(_._1).distinct
    val pat = "(?<=-)(OK|RP|RT|OB)(?=-)".r

    val v = ventas.map { x =>
      val vs = jsonMapper.toJsonString(x)
      val vNode = jsonMapper.getJsonNode(vs)

      val fecha = arch.find(a => a._1 == x).map(_._3)
      val audi = arch.find(a => a._1 == x).map(_._2)

      val es = (pat findFirstIn audi.get.audio1).get
      jsonMapper.putElement(vNode, "fechaCreacion", fecha.get.toIsoDateString())
      jsonMapper.putElement(vNode, "estado", es)
      val as = jsonMapper.toJsonString(audi)
      val aNode = jsonMapper.getJsonNode(as)

      jsonMapper.addNode("auditoria", aNode, vNode)

      vNode

    }
    Ok(jsonMapper.toJson(v))

  }

  def estadisticaRechazos = authAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val fdesde = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaDesde")
    val fhasta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaHasta")
    val fechaDesde = DateTime.fromIsoDateTimeString(fdesde).get
    val fechaHasta = DateTime.fromIsoDateTimeString(fhasta).get

    val future = EstadisticaRepository.rechazos(fechaDesde, fechaHasta)
    val arch = Await.result(future, Duration.Inf)

    val v = arch.map { case (fechaCreacion, nombre, cuil, estado, fechaR, obs, recuperable, vendedor) =>

      val node = jsonMapper.mapper.createObjectNode()
      jsonMapper.putElement(node, "nombre", nombre)
      jsonMapper.putElement(node, "fechaCreacion", fechaCreacion)
      jsonMapper.putElement(node, "cuil", cuil)
      jsonMapper.putElement(node, "estado", estado)
      jsonMapper.putElement(node, "fechaRechazo", fechaR)
      jsonMapper.putElement(node, "observacion", obs)
      if (recuperable == "0")
        jsonMapper.putElement(node, "tipo", "Total")
      else
        jsonMapper.putElement(node, "tipo", "Parcial")
      jsonMapper.putElement(node, "vendedor", vendedor)

      node
    }

    Ok(jsonMapper.toJson(v))
  }

  def estadisticaVendedoras = authAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val fdesde = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaDesde")
    val fhasta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaHasta")
    val fechaDesde = DateTime.fromIsoDateTimeString(fdesde).get
    val fechaHasta = DateTime.fromIsoDateTimeString(fhasta).get
    val future = EstadisticaRepository.cantidadVentasPerfil(fechaDesde, fechaHasta, VENDEDORA)
    val arch = Await.result(future, Duration.Inf)

    val v = arch.map { case (nombre, rechazadas, presentadas, pagadas) =>

      val node = jsonMapper.mapper.createObjectNode()

      jsonMapper.putElement(node, "nombre", nombre)
      jsonMapper.putElement(node, "rechazadas", rechazadas)
      jsonMapper.putElement(node, "presentadas", presentadas)
      jsonMapper.putElement(node, "pagadas", pagadas)

      node
    }

    Ok(jsonMapper.toJson(v))
  }

  def estadisticaCall = authAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val fdesde = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaDesde")
    val fhasta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaHasta")
    val fechaDesde = DateTime.fromIsoDateTimeString(fdesde).get
    val fechaHasta = DateTime.fromIsoDateTimeString(fhasta).get
    val future = EstadisticaRepository.cantidadVentasCall(fechaDesde, fechaHasta, OPERADOR_VENTA)
    val arch = Await.result(future, Duration.Inf)

    val v = arch.map { case (nombre, rechazadas, presentadas, pagadas, auditadas) =>

      val node = jsonMapper.mapper.createObjectNode()

      jsonMapper.putElement(node, "nombre", nombre)
      jsonMapper.putElement(node, "rechazadas", rechazadas)
      jsonMapper.putElement(node, "presentadas", presentadas)
      jsonMapper.putElement(node, "pagadas", pagadas)
      jsonMapper.putElement(node, "auditadas", auditadas)

      node
    }

    Ok(jsonMapper.toJson(v))
  }

  def estadisticaEmpresas = authAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val fdesde = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaDesde")
    val fhasta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaHasta")
    val fechaDesde = DateTime.fromIsoDateTimeString(fdesde).get
    val fechaHasta = DateTime.fromIsoDateTimeString(fhasta).get

    val future = EstadisticaRepository.empresas(fechaDesde, fechaHasta)
    val arch = Await.result(future, Duration.Inf)

    val v = arch.map { case (idVenta, empresa, direccion, localidad, cantidadEmpleados, hora_entrada, hora_salida, vendedor) =>

      val node = jsonMapper.mapper.createObjectNode()
      jsonMapper.putElement(node, "empresa", empresa)
      jsonMapper.putElement(node, "direccion", localidad)
      jsonMapper.putElement(node, "cantidadEmpleados", cantidadEmpleados)
      jsonMapper.putElement(node, "horaEntrada", hora_entrada)
      jsonMapper.putElement(node, "horaSalida", hora_salida)
      jsonMapper.putElement(node, "localidad", localidad)
      jsonMapper.putElement(node, "vendedor", vendedor)

      node
    }

    Ok(jsonMapper.toJson(v))
  }

  def localidadesEmpresa = authAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val fdesde = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaDesde")
    val fhasta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaHasta")
    val fechaDesde = DateTime.fromIsoDateTimeString(fdesde).get
    val fechaHasta = DateTime.fromIsoDateTimeString(fhasta).get

    val future2 = EstadisticaRepository.localidadesEmpresa(fechaDesde, fechaHasta)
    val arch2 = Await.result(future2, Duration.Inf)
    Ok(jsonMapper.toJson(arch2))
    val v2 = arch2.map { case (localidad) =>

      val node2 = jsonMapper.mapper.createObjectNode()
      jsonMapper.putElement(node2, "localidad", localidad)


      node2
    }
    Ok(jsonMapper.toJson(v2))
  }

  def zonas = getAuthAction {implicit request =>
    val future = EstadisticaRepository.zonas
    val estados = Await.result(future, Duration.Inf)

    Ok(jsonMapper.toJson(estados))
  }

  def estadisticaPromotoras = authAction { implicit request =>

    implicit val obs: Seq[String] = request.obrasSociales
    val fdesde = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaDesde")
    val fhasta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaHasta")
    val fechaDesde = DateTime.fromIsoDateTimeString(fdesde).get
    val fechaHasta = DateTime.fromIsoDateTimeString(fhasta).get
    val future = EstadisticaRepository.cantidadVentasPerfil(fechaDesde, fechaHasta, PROMOTORA)
    val arch = Await.result(future, Duration.Inf)

    val v = arch.map { case (nombre, rechazadas, presentadas, pagadas) =>

      val node = jsonMapper.mapper.createObjectNode()
      jsonMapper.putElement(node, "nombre", nombre)
      jsonMapper.putElement(node, "rechazadas", rechazadas)
      jsonMapper.putElement(node, "presentadas", presentadas)
      jsonMapper.putElement(node, "pagadas", pagadas)

      node
    }

    Ok(jsonMapper.toJson(v))
  }

  def estadisticaCantidadVentasPorObraSocial = authAction { implicit request =>

    implicit val obs: Seq[String] = request.obrasSociales
    val fdesde = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaDesde")
    val fhasta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaHasta")
    val fechaDesde = DateTime.fromIsoDateTimeString(fdesde).get
    val fechaHasta = DateTime.fromIsoDateTimeString(fhasta).get
    val future = EstadisticaRepository.cantidadVentasTotalPorObraSocial(fechaDesde, fechaHasta)
    val arch = Await.result(future, Duration.Inf)

    val v = arch.map { case (fecha, rechazadas, presentadas, pagadas) =>

      val node = jsonMapper.mapper.createObjectNode()

      jsonMapper.putElement(node, "fecha", fecha )
      jsonMapper.putElement(node, "rechazadas", rechazadas)
      jsonMapper.putElement(node, "presentadas", presentadas)
      jsonMapper.putElement(node, "pagadas", pagadas)

      node
    }
    Ok(jsonMapper.toJson(v))

  }

  def estaditicaCantidadVentasTotalesPorDia = authAction { implicit request =>

    implicit val obs: Seq[String] = request.obrasSociales
    val fdesde = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaDesde")
    val fhasta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaHasta")
    val fechaDesde = DateTime.fromIsoDateTimeString(fdesde).get
    val fechaHasta = DateTime.fromIsoDateTimeString(fhasta).get
    val future = EstadisticaRepository.cantidadVentasTotalPorDia(fechaDesde, fechaHasta)
    val arch = Await.result(future, Duration.Inf)



    val v = arch.map { case (fecha, rechazadas, presentadas, pagadas) =>

      val node = jsonMapper.mapper.createObjectNode()

      jsonMapper.putElement(node, "fecha", fecha )
      jsonMapper.putElement(node, "rechazadas", rechazadas)
      jsonMapper.putElement(node, "presentadas", presentadas)
      jsonMapper.putElement(node, "pagadas", pagadas)

      node
    }
    Ok(jsonMapper.toJson(v))

  }

  def estaditicaCantidadVentasTotalesPorSemana = authAction { implicit request =>

    implicit val obs: Seq[String] = request.obrasSociales
    val fdesde = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaDesde")
    val fhasta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaHasta")
    val fechaDesde = DateTime.fromIsoDateTimeString(fdesde).get
    val fechaHasta = DateTime.fromIsoDateTimeString(fhasta).get
    val future = EstadisticaRepository.cantidadVentasTotalPorSemana(fechaDesde, fechaHasta)
    val arch = Await.result(future, Duration.Inf)



    val v = arch.map { case (fecha, rechazadas, presentadas, pagadas) =>

      val node = jsonMapper.mapper.createObjectNode()

      jsonMapper.putElement(node, "semana", fecha )
      jsonMapper.putElement(node, "rechazadas", rechazadas)
      jsonMapper.putElement(node, "presentadas", presentadas)
      jsonMapper.putElement(node, "pagadas", pagadas)

      node
    }
    Ok(jsonMapper.toJson(v))

  }

  def estaditicaCantidadVentasTotalesPorMes = authAction { implicit request =>

    implicit val obs: Seq[String] = request.obrasSociales
    val fdesde = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaDesde")
    val fhasta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaHasta")
    val fechaDesde = DateTime.fromIsoDateTimeString(fdesde).get
    val fechaHasta = DateTime.fromIsoDateTimeString(fhasta).get
    val future = EstadisticaRepository.cantidadVentasTotalPorMes(fechaDesde, fechaHasta)
    val arch = Await.result(future, Duration.Inf)



    val v = arch.map { case (fecha, rechazadas, presentadas, pagadas) =>

      val node = jsonMapper.mapper.createObjectNode()

      jsonMapper.putElement(node, "mes", fecha )
      jsonMapper.putElement(node, "rechazadas", rechazadas)
      jsonMapper.putElement(node, "presentadas", presentadas)
      jsonMapper.putElement(node, "pagadas", pagadas)

      node
    }
    Ok(jsonMapper.toJson(v))

  }

  def estaditicaCantidadVentasTotalesPorAnio = authAction { implicit request =>

    implicit val obs: Seq[String] = request.obrasSociales
    val fdesde = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaDesde")
    val fhasta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaHasta")
    val fechaDesde = DateTime.fromIsoDateTimeString(fdesde).get
    val fechaHasta = DateTime.fromIsoDateTimeString(fhasta).get
    val future = EstadisticaRepository.cantidadVentasTotalPorAnio(fechaDesde, fechaHasta)
    val arch = Await.result(future, Duration.Inf)



    val v = arch.map { case (fecha, rechazadas, presentadas, pagadas) =>

      val node = jsonMapper.mapper.createObjectNode()

      jsonMapper.putElement(node, "año", fecha )
      jsonMapper.putElement(node, "rechazadas", rechazadas)
      jsonMapper.putElement(node, "presentadas", presentadas)
      jsonMapper.putElement(node, "pagadas", pagadas)

      node
    }
    Ok(jsonMapper.toJson(v))

  }

  def estadisticaCantidadVentasPorZona = authAction { implicit request =>
      implicit val obs: Seq[String] = request.obrasSociales
      val fdesde = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaDesde")
      val fhasta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaHasta")
      val fechaDesde = DateTime.fromIsoDateTimeString(fdesde).get
      val fechaHasta = DateTime.fromIsoDateTimeString(fhasta).get
      val future = EstadisticaRepository.cantidadVentasPorZona(fechaDesde, fechaHasta)
      val arch = Await.result(future, Duration.Inf)

      val v = arch.map { case (zona, rechazadas, presentadas, pagadas) =>

        val node = jsonMapper.mapper.createObjectNode()

        jsonMapper.putElement(node, "zona", zona)
        jsonMapper.putElement(node, "rechazadas", rechazadas)
        jsonMapper.putElement(node, "presentadas", presentadas)
        jsonMapper.putElement(node, "pagadas", pagadas)

        node
      }


    Ok(jsonMapper.toJson(v))
  }

  def estadisticaCantidadVentasPorLocalidad = authAction { implicit request =>
      implicit val obs: Seq[String] = request.obrasSociales
      val fdesde = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaDesde")
      val fhasta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaHasta")
      val zonas = jsonMapper.fromJson[Seq[String]](request.rootNode.get("zonas").toString)
      val fechaDesde = DateTime.fromIsoDateTimeString(fdesde).get
      val fechaHasta = DateTime.fromIsoDateTimeString(fhasta).get
      val future = EstadisticaRepository.cantidadVentasPorLocalidad(fechaDesde, fechaHasta, zonas)
      val arch = Await.result(future, Duration.Inf)

      val v = arch.map { case (localidad, rechazadas, presentadas, pagadas) =>

        val node = jsonMapper.mapper.createObjectNode()

        jsonMapper.putElement(node, "localidad", localidad)
        jsonMapper.putElement(node, "rechazadas", rechazadas)
        jsonMapper.putElement(node, "presentadas", presentadas)
        jsonMapper.putElement(node, "pagadas", pagadas)

        node
      }


      Ok(jsonMapper.toJson(v))
    }

  def estadisticaCantVisitasPromotora = authAction { implicit request =>

    val fdesde = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaDesde")
    val fhasta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaHasta")
    val fechaDesde = DateTime.fromIsoDateTimeString(fdesde).get
    val fechaHasta = DateTime.fromIsoDateTimeString(fhasta).get
    val future = EstadisticaRepository.cantidadVisitasPerfil(fechaDesde, fechaHasta, PROMOTORA)
    val arch = Await.result(future, Duration.Inf)

    val v = arch.map { case (nombre, cantidad) =>

      val node = jsonMapper.mapper.createObjectNode()
      jsonMapper.putElement(node, "nombre", nombre)
      jsonMapper.putElement(node, "cantidad", cantidad)

      node
    }

    Ok(jsonMapper.toJson(v))
  }

  def estadisticaEficienciaPromotora = authAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales

    val fdesde = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaDesde")
    val fhasta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaHasta")
    val fechaDesde = DateTime.fromIsoDateTimeString(fdesde).get
    val fechaHasta = DateTime.fromIsoDateTimeString(fhasta).get
    val future = EstadisticaRepository.cantidadVentasPerfil(fechaDesde, fechaHasta, PROMOTORA)
    val arch = Await.result(future, Duration.Inf)


    val total = arch.map(x => x._2+x._3+x._4).foldLeft(0) {_ + _}
    val v = arch.map { case (nombre, rechazadas, presentadas, pagadas) =>

      val node = jsonMapper.mapper.createObjectNode()
      jsonMapper.putElement(node, "nombre", nombre)
      jsonMapper.putElement(node, "rechazadas", truncateAt(math.rint(rechazadas.toDouble/total * 1000)/1000 * 100,2))
      jsonMapper.putElement(node, "presentadas", truncateAt( math.rint(presentadas.toDouble/total * 1000)/1000 * 100,2))
      jsonMapper.putElement(node, "pagadas", truncateAt(math.rint(pagadas.toDouble/total * 1000) / 1000 * 100,2))

      node
    }

    Ok(jsonMapper.toJson(v))
  }

  def estadistiscaEficienciaExterno = authAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales

    val fdesde = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaDesde")
    val fhasta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaHasta")
    val fechaDesde = DateTime.fromIsoDateTimeString(fdesde).get
    val fechaHasta = DateTime.fromIsoDateTimeString(fhasta).get
    val future = EstadisticaRepository.cantidadVentasPerfil(fechaDesde, fechaHasta, EXTERNO)
    val arch = Await.result(future, Duration.Inf)


    val total = arch.map(x => x._2+x._3+x._4).foldLeft(0) {_ + _}
    val v = arch.map { case (nombre, rechazadas, presentadas, pagadas) =>

      val node = jsonMapper.mapper.createObjectNode()
      jsonMapper.putElement(node, "nombre", nombre)
      jsonMapper.putElement(node, "rechazadas", truncateAt(math.rint(rechazadas.toDouble/total * 1000)/1000 * 100,2))
      jsonMapper.putElement(node, "presentadas", truncateAt( math.rint(presentadas.toDouble/total * 1000)/1000 * 100,2))
      jsonMapper.putElement(node, "pagadas", truncateAt(math.rint(pagadas.toDouble/total * 1000) / 1000 * 100,2))

      node
    }

    Ok(jsonMapper.toJson(v))
  }

  def estadistiscaEficienciaVendedora = authAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales

    val fdesde = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaDesde")
    val fhasta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaHasta")
    val fechaDesde = DateTime.fromIsoDateTimeString(fdesde).get
    val fechaHasta = DateTime.fromIsoDateTimeString(fhasta).get
    val future = EstadisticaRepository.cantidadVentasPerfil(fechaDesde, fechaHasta, VENDEDORA)
    val arch = Await.result(future, Duration.Inf)



    val total = arch.map(x => x._2+x._3+x._4).foldLeft(0) {_ + _}
    val v = arch.map { case (nombre, rechazadas, presentadas, pagadas) =>

      val node = jsonMapper.mapper.createObjectNode()
      jsonMapper.putElement(node, "nombre", nombre)
      jsonMapper.putElement(node, "rechazadas", truncateAt(math.rint(rechazadas.toDouble/total * 1000)/1000 * 100,2))
      jsonMapper.putElement(node, "presentadas", truncateAt( math.rint(presentadas.toDouble/total * 1000)/1000 * 100,2))
      jsonMapper.putElement(node, "pagadas", truncateAt(math.rint(pagadas.toDouble/total * 1000) / 1000 * 100,2))

      node
    }

    Ok(jsonMapper.toJson(v))
  }

  def estadistiscaEficienciaCall = authAction { implicit request =>
    implicit val obs: Seq[String] = request.obrasSociales
    val fdesde = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaDesde")
    val fhasta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaHasta")
    val fechaDesde = DateTime.fromIsoDateTimeString(fdesde).get
    val fechaHasta = DateTime.fromIsoDateTimeString(fhasta).get
    val future = EstadisticaRepository.cantidadVentasPerfil(fechaDesde, fechaHasta, OPERADOR_VENTA)
    val arch = Await.result(future, Duration.Inf)

    val total = arch.map(x => x._2+x._3+x._4).foldLeft(0) {_ + _}
    val v = arch.map { case (nombre, rechazadas, presentadas, pagadas) =>

      val node = jsonMapper.mapper.createObjectNode()
      jsonMapper.putElement(node, "nombre", nombre)
      jsonMapper.putElement(node, "rechazadas", truncateAt(math.rint(rechazadas.toDouble/total * 1000)/1000 * 100,2))
      jsonMapper.putElement(node, "presentadas", truncateAt( math.rint(presentadas.toDouble/total * 1000)/1000 * 100,2))
      jsonMapper.putElement(node, "pagadas", truncateAt(math.rint(pagadas.toDouble/total * 1000) / 1000 * 100,2))

      node
    }

    Ok(jsonMapper.toJson(v))
  }

  def estadisticaExterno = authAction { implicit request =>

    implicit val obs: Seq[String] = request.obrasSociales
    val fdesde = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaDesde")
    val fhasta = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(request.rootNode, "fechaHasta")
    val fechaDesde = DateTime.fromIsoDateTimeString(fdesde).get
    val fechaHasta = DateTime.fromIsoDateTimeString(fhasta).get
    val future = EstadisticaRepository.cantidadVentasPerfil(fechaDesde, fechaHasta, EXTERNO)
    val arch = Await.result(future, Duration.Inf)

    val v = arch.map { case (nombre, rechazadas, presentadas, pagadas) =>

      val node = jsonMapper.mapper.createObjectNode()
      jsonMapper.putElement(node, "nombre", nombre)
      jsonMapper.putElement(node, "rechazadas", rechazadas)
      jsonMapper.putElement(node, "presentadas", presentadas)
      jsonMapper.putElement(node, "pagadas", pagadas)

      node
    }

    Ok(jsonMapper.toJson(v))
  }

  def indicadorVentasPresentadasDelMes = getAuthAction { implicit request =>

    implicit val obs: Seq[String] = request.obrasSociales
    val future = EstadisticaRepository.indicadorVentasPresentadasDelMes
    val arch = Await.result(future, Duration.Inf)

    val v = arch.map { case (presentadas, pagadas, rechazadas, presentadasMesAnterior, pagadasMesAnterior, rechazadasMesAnterior) =>

      val node = jsonMapper.mapper.createObjectNode()
      jsonMapper.putElement(node, "presentadas", presentadas)
      jsonMapper.putElement(node, "pagadas", pagadas)
      jsonMapper.putElement(node, "rechazadas", rechazadas)
      jsonMapper.putElement(node, "presentadasMesAnterior", presentadasMesAnterior)
      jsonMapper.putElement(node, "pagadasMesAnterior", pagadasMesAnterior)
      jsonMapper.putElement(node, "rechazadasMesAnterior", rechazadasMesAnterior)

      node
    }.head

    Ok(jsonMapper.toJson(v))
  }

  def truncateAt(n: Double, p: Int): Double = {
    val s = math pow (10, p); (math floor n * s) / s
  }

}
