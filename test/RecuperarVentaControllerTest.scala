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
class RecuperarVentaControllerTest extends PlaySpec with GuiceOneAppPerSuite with Estados {


  "RecuperarVentaController" should {

    "ventas recuperables" in {
      Db.inicializarDb

      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(437, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(438, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
      )
      val estadosEsperados = Seq(
        Estado("200", 1, VISITA_CONFIRMADA, DateTime.now),
        Estado("200", 2, RECHAZO_LOGISTICA, DateTime.now, true),
        Estado("200", 3, RECHAZO_AUDITORIA, DateTime.now, true),
        Estado("200", 4, RECHAZO_VALIDACION, DateTime.now, true, Some("cantidad impagos 25"), 1, true),
        Estado("200", 5, RECHAZO_VALIDACION, DateTime.now, true, Some("meses de traspaso 2"), 1, true),
      )


      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)


      val Some(result) = route(app, FakeRequest(GET, "/recuperarVenta/all").withHeaders("My-Authorization" -> Token.header))
      val cantidadFilas = contentAsJson(result).asInstanceOf[JsArray].value.length

      assert(cantidadFilas == 2)
      status(result) mustBe OK

    }
    "ventas para poder marcarlas para que se puedan recueprar en un futuro" in {
      Db.inicializarDb

      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(437, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(438, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
      )
      val estadosEsperados = Seq(
        Estado("200", 1, VISITA_CONFIRMADA, DateTime.now),
        Estado("200", 2, RECHAZO_LOGISTICA, DateTime.now, true),
        Estado("200", 3, RECHAZO_AUDITORIA, DateTime.now, true),
        Estado("200", 4, RECHAZO_VALIDACION, DateTime.now, true, Some("cantidad impagos 25"),1, true),
        Estado("200", 5, RECHAZO_VALIDACION, DateTime.now, true, Some("meses de traspaso 2"),2, true),
      )


      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)


      val Some(result) = route(app, FakeRequest(GET, "/recuperarVenta/ventasParaPoderRecuperar").withHeaders("My-Authorization" -> Token.header))
      val cantidadFilas = contentAsJson(result).asInstanceOf[JsArray].value.length

      assert(cantidadFilas == 2)
      status(result) mustBe OK

    }



    "recuperar venta" in {
      Db.inicializarDb
      val json = Json.parse(
        """
        {
          "idVenta": 1,
          "dni": 432,
          "idEstado": 1
        }
        """)

      val jsonMapper = new JsonMapper
      val node = jsonMapper.getJsonNode(json.toString)
      val idEstado = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(node, "idEstado")


      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(437, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(438, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
      )
      val estadosEsperados = Seq(
        Estado("200", 1, RECHAZO_LOGISTICA, DateTime.now, true),
        Estado("200", 2, VISITA_CONFIRMADA, DateTime.now),
        Estado("200", 3, VISITA_CONFIRMADA, DateTime.now),
        Estado("200", 4, RECHAZO_VALIDACION, DateTime.now),
        Estado("200", 5, VALIDADO, DateTime.now),
      )


      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)


      val Some(result) = route(app, FakeRequest(POST, "/recuperarVenta/recuperar").withJsonBody(json).withHeaders("My-Authorization" -> Token.header))
      val cantidadFilas = contentAsString(result)

      val estadoEsperado = Db.runWithAwait(estados.filter(x => x.idVenta === 1.toLong && x.estado === RECHAZO_LOGISTICA).result.headOption)
      assert(!estadoEsperado.isDefined)
      status(result) mustBe OK

    }

    "marcar venta para ser recuperada" in {
      Db.inicializarDb
      val json = Json.parse(
        """
        {
          "idVenta": 1,
          "dni": 432,
          "idEstado": 1
        }
        """)

      val jsonMapper = new JsonMapper
      val node = jsonMapper.getJsonNode(json.toString)
      val idEstado = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(node, "idEstado")


      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(437, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(438, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
      )
      val estadosEsperados = Seq(
        Estado("200", 1, RECHAZO_LOGISTICA, DateTime.now, true),
        Estado("200", 2, VISITA_CONFIRMADA, DateTime.now),
        Estado("200", 3, VISITA_CONFIRMADA, DateTime.now),
        Estado("200", 4, RECHAZO_VALIDACION, DateTime.now),
        Estado("200", 5, VALIDADO, DateTime.now),
      )


      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)


      val Some(result) = route(app, FakeRequest(POST, "/recuperarVenta/marcarParaRecuperar").withJsonBody(json).withHeaders("My-Authorization" -> Token.header))
      val cantidadFilas = contentAsString(result)

      val estadoEsperado = Db.runWithAwait(estados.filter(x => x.idVenta === 1.toLong && x.estado === RECHAZO_LOGISTICA).result.head)
      assert(estadoEsperado.paraRecuperar)
      status(result) mustBe OK

    }


    "rechazar venta" in {
      Db.inicializarDb
      val json = Json.parse(
        """
        {
          "idVenta": 1,
          "dni": 432,
          "idEstado": 1
        }
        """)

      val jsonMapper = new JsonMapper
      val node = jsonMapper.getJsonNode(json.toString)
      val idEstado = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(node, "idEstado")


      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(437, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(438, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
      )
      val estadosEsperados = Seq(
        Estado("200", 1, RECHAZO_LOGISTICA, DateTime.now, true),
        Estado("200", 2, VISITA_CONFIRMADA, DateTime.now),
        Estado("200", 3, VISITA_CONFIRMADA, DateTime.now),
        Estado("200", 4, RECHAZO_VALIDACION, DateTime.now),
        Estado("200", 5, VALIDADO, DateTime.now),
      )


      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)


      val Some(result) = route(app, FakeRequest(POST, "/recuperarVenta/rechazo").withJsonBody(json).withHeaders("My-Authorization" -> Token.header))
      val cantidadFilas = status(result)

      val estadoEsperado = Db.runWithAwait(estados.filter(x => x.idVenta === 1.toLong && x.estado === RECHAZO_LOGISTICA).result.head)
      assert(estadoEsperado.recuperable == false)
      status(result) mustBe OK

    }
  }

}