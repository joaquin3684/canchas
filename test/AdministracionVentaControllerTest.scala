import akka.http.scaladsl.model.DateTime
import models._
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsArray, JsValue, Json}
import play.api.test.Helpers._
import play.api.test.FakeRequest
import repositories.Db
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas.{estados, auditorias, ventas, validaciones}
import services.JsonMapper

/**
  * Unit tests can run without a full Play application.
  */
class AdministracionVentaControllerTest extends PlaySpec with GuiceOneAppPerSuite with Estados {


  "AdministracionVentaControllerTest" should {

    "ventas incompletas" in {
      Db.inicializarDb

      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(437, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(438, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
      )
      val estadosEsperados = Seq(
        Estado("200", 432, VISITA_CONFIRMADA, DateTime.now),
        Estado("200", 435, VISITA_CONFIRMADA, DateTime.now),
        Estado("200", 436, VISITA_CONFIRMADA, DateTime.now),
        Estado("200", 437, RECHAZO_VALIDACION, DateTime.now),
        Estado("200", 438, VALIDADO, DateTime.now),
      )


      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)


      val Some(result) = route(app, FakeRequest(GET, "/administracionVenta/ventasIncompletas").withHeaders("My-Authorization" -> Token.header))
      val cantidadFilas = contentAsJson(result).asInstanceOf[JsArray].value.length

      assert(cantidadFilas == 3)
      status(result) mustBe OK

    }

    "completar venta" in {
      Db.inicializarDb
      val json = Json.parse(
        """
        {
          "dni": 432,
          "empresa": "200",
          "cuit": 200,
          "tresPorciento": 200.32
        }
        """)

      val jsonMapper = new JsonMapper
      val node = jsonMapper.getJsonNode(json.toString)
      val dni = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(node, "dni")
      val jempresa = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(node, "empresa")
      val jcuit = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(node, "cuit").toInt
      val jtresPorciento = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(node, "tresPorciento").toDouble


      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(437, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(438, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
      )
      val estadosEsperados = Seq(
        Estado("200", 432, VISITA_CONFIRMADA, DateTime.now),
        Estado("200", 435, VISITA_CONFIRMADA, DateTime.now),
        Estado("200", 436, VISITA_CONFIRMADA, DateTime.now),
        Estado("200", 437, RECHAZO_VALIDACION, DateTime.now),
        Estado("200", 438, VALIDADO, DateTime.now),
      )



      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)


      val Some(result) = route(app, FakeRequest(POST, "/administracionVenta/completarVenta").withJsonBody(json).withHeaders("My-Authorization" -> Token.header))
      val cantidadFilas = contentAsString(result)

      val ventaObtenida = Db.runWithAwait(ventas.filter(_.dni === 432).result.head)
      val ventaEsperada = ventaObtenida.copy(empresa = Some(jempresa), cuit = Some(jcuit), tresPorciento = Some(jtresPorciento))
      assert(ventaObtenida == ventaEsperada)
      status(result) mustBe OK

    }
    "ventas presentables" in {
      Db.inicializarDb

      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None, Some("asdf"), Some(32), Some(45.3)),
        Venta(435, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None, Some("asdf"), Some(32), Some(45.3)),
        Venta(436, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None, Some("asdf"), Some(32), Some(45.3)),
        Venta(437, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(438, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
      )
      val estadosEsperados = Seq(
        Estado("200", 432, VISITA_CONFIRMADA, DateTime.now),
        Estado("200", 432, CREADO, DateTime.now),
        Estado("200", 435, VISITA_CONFIRMADA, DateTime.now),
        Estado("200", 435, CREADO, DateTime.now),
        Estado("200", 436, VISITA_CONFIRMADA, DateTime.now),
        Estado("200", 436, PRESENTADA, DateTime.now),
        Estado("200", 438, VALIDADO, DateTime.now),
      )

      val validacionesEsperadas = Seq(
        Validacion(432, true, true, true, 4, None, None, None),
        Validacion(435, true, true, true, 4, None, None, None),
        Validacion(436, true, true, true, 4, None, None, None),
      )

      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)
      Db.runWithAwait(validaciones ++= validacionesEsperadas)


      val Some(result) = route(app, FakeRequest(GET, "/administracionVenta/ventasPresentables").withHeaders("My-Authorization" -> Token.header))
      val cantidadFilas = contentAsJson(result).asInstanceOf[JsArray].value.length

      assert(cantidadFilas == 2)
      status(result) mustBe OK

    }

    "presentar ventas" in {
      Db.inicializarDb
      val json = Json.parse(
        """
           {
            "dnis": [ 432, 435, 436 ],
            "fechaPresentacion": "2018-02-04T20:20:10"
            }

        """)
      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(437, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(438, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
      )

      Db.runWithAwait(ventas ++= ventasEsperadas)

      val estadosEsperados = Seq(
        Estado("200", 432, PRESENTADA, DateTime.now, false, None, 1),
        Estado("200", 435, PRESENTADA, DateTime.now, false, None, 2),
        Estado("200", 436, PRESENTADA, DateTime.now, false, None, 3),
        Estado("200", 435, CREADO, DateTime.now),
        Estado("200", 432, CREADO, DateTime.now),
        Estado("200", 438, VALIDADO, DateTime.now),
      )

      val Some(result) = route(app, FakeRequest(POST, "/administracionVenta/presentarVentas").withJsonBody(json).withHeaders("My-Authorization" -> Token.header))
      val cantidadFilas = contentAsString(result)

      val estadosObtenidos = Db.runWithAwait(estados.filter(_.estado === PRESENTADA).result).toList

      assert(estadosObtenidos.map(x => (x.user, x.dni, x.estado, x.observacion, x.id)) == estadosEsperados.take(3).map(x => (x.user, x.dni, x.estado, x.observacion, x.id)))
      status(result) mustBe OK

    }

    "ventas presentadas" in {
      Db.inicializarDb

      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(437, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(438, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
      )
      val estadosEsperados = Seq(
        Estado("200", 432, PRESENTADA, DateTime.now, false, None, 1),
        Estado("200", 435, PRESENTADA, DateTime.now, false, None, 2),
        Estado("200", 436, PRESENTADA, DateTime.now, false, None, 3),
        Estado("200", 436, PAGADA, DateTime.now),
        Estado("200", 432, CREADO, DateTime.now),
        Estado("200", 438, VALIDADO, DateTime.now),
      )

      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)

      val Some(result) = route(app, FakeRequest(GET, "/administracionVenta/ventasPresentadas").withHeaders("My-Authorization" -> Token.header))
      val cantidadFilas = contentAsJson(result).asInstanceOf[JsArray].value.length

      assert(cantidadFilas == 2)
      status(result) mustBe OK

    }

    "analizar presentacion" in {
      Db.inicializarDb

      val jsonPagada = Json.parse(
        """
           {
              "dni": 432,
              "estado": "pagada",
              "observacion": null,
              "fechaPresentacion": null
            }

        """)

      val jsonRechazada = Json.parse(
        """
           {
              "dni": 435,
              "estado": "rechazada",
              "observacion": "estooo",
              "fechaPresentacion": null
            }

        """)

      val jsonPendiente = Json.parse(
        """
           {
              "dni": 436,
              "estado": "pendiente auditoria",
              "observacion": null,
              "fechaPresentacion": "2017-02-03T20:20:20"
            }

        """)

      val jsonMapper = new JsonMapper
      val pendienteNode = jsonMapper.getJsonNode(jsonPendiente.toString)
      val fechaPresentacionNueva = DateTime.fromIsoDateTimeString(jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(pendienteNode, "fechaPresentacion")).get


      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(437, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(438, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
      )
      val estadosEsperados = Seq(
        Estado("200", 432, PRESENTADA, DateTime.now, false, None, 1),
        Estado("200", 435, PRESENTADA, DateTime.now, false, None, 2),
        Estado("200", 436, PRESENTADA, DateTime.now, false, None, 3),
        Estado("200", 432, CREADO, DateTime.now),
        Estado("200", 438, VALIDADO, DateTime.now),
      )

      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)

      val Some(result1) = route(app, FakeRequest(POST, "/administracionVenta/analizarPresentacion").withJsonBody(jsonPagada).withHeaders("My-Authorization" -> Token.header))
      val pagada = contentAsString(result1)

      val Some(result2) = route(app, FakeRequest(POST, "/administracionVenta/analizarPresentacion").withJsonBody(jsonRechazada).withHeaders("My-Authorization" -> Token.header))
      val rechazada = contentAsString(result2)

      val Some(result3) = route(app, FakeRequest(POST, "/administracionVenta/analizarPresentacion").withJsonBody(jsonPendiente).withHeaders("My-Authorization" -> Token.header))
      val pendiente = contentAsString(result3)

      val estadoPagadoObtenido = Db.runWithAwait(estados.filter(_.estado === PAGADA).result.head)
      val estadoRechazadoObtenido = Db.runWithAwait(estados.filter(_.estado === RECHAZO_ADMINISTRACION).result.head)
      val estadoPresentadoObtenido = Db.runWithAwait(estados.filter(x => x.estado === PRESENTADA && x.dni === 436).result.head)

      val estadoPagadoEsperado = Estado("200", 432, PAGADA, DateTime.now, false, None, estadoPagadoObtenido.id)
      val estadoRechazadoEsperado = Estado("200", 435, RECHAZO_ADMINISTRACION, DateTime.now, false, Some("estooo"), estadoRechazadoObtenido.id)
      val estadoPresentadoEsperado = Estado("200", 436, PRESENTADA, fechaPresentacionNueva, false, None, estadoPresentadoObtenido.id)

      assert(estadoPagadoObtenido == estadoPagadoEsperado)
      assert(estadoRechazadoObtenido == estadoRechazadoEsperado)
      assert(estadoPresentadoObtenido == estadoPresentadoEsperado)
      status(result1) mustBe OK
      status(result2) mustBe OK
      status(result3) mustBe OK

    }
  }

}