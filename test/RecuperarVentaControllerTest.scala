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
        Estado("200", 432, VISITA_CONFIRMADA, DateTime.now),
        Estado("200", 435, RECHAZO_LOGISTICA, DateTime.now, true),
        Estado("200", 436, RECHAZO_AUDITORIA, DateTime.now, true),
        Estado("200", 437, RECHAZO_VALIDACION, DateTime.now, true, Some("cantidad impagos 25")),
        Estado("200", 438, RECHAZO_VALIDACION, DateTime.now, true, Some("meses de traspaso 2")),
      )


      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)


      val Some(result) = route(app, FakeRequest(GET, "/recuperarVenta/all").withHeaders("My-Authorization" -> Token.header))
      val cantidadFilas = contentAsJson(result).asInstanceOf[JsArray].value.length

      assert(cantidadFilas == 3)
      status(result) mustBe OK

    }

    "recuperar venta" in {
      Db.inicializarDb
      val json = Json.parse(
        """
        {
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
        Estado("200", 432, RECHAZO_LOGISTICA, DateTime.now, true),
        Estado("200", 435, VISITA_CONFIRMADA, DateTime.now),
        Estado("200", 436, VISITA_CONFIRMADA, DateTime.now),
        Estado("200", 437, RECHAZO_VALIDACION, DateTime.now),
        Estado("200", 438, VALIDADO, DateTime.now),
      )


      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)


      val Some(result) = route(app, FakeRequest(POST, "/recuperarVenta/recuperar").withJsonBody(json).withHeaders("My-Authorization" -> Token.header))
      val cantidadFilas = contentAsString(result)

      val estadoEsperado = Db.runWithAwait(estados.filter(x => x.dni === 432 && x.estado === RECHAZO_LOGISTICA).result.headOption)
      assert(!estadoEsperado.isDefined)
      status(result) mustBe OK

    }

    "rechazar venta" in {
      Db.inicializarDb
      val json = Json.parse(
        """
        {
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
        Estado("200", 432, RECHAZO_LOGISTICA, DateTime.now, true),
        Estado("200", 435, VISITA_CONFIRMADA, DateTime.now),
        Estado("200", 436, VISITA_CONFIRMADA, DateTime.now),
        Estado("200", 437, RECHAZO_VALIDACION, DateTime.now),
        Estado("200", 438, VALIDADO, DateTime.now),
      )


      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)


      val Some(result) = route(app, FakeRequest(POST, "/recuperarVenta/rechazo").withJsonBody(json).withHeaders("My-Authorization" -> Token.header))
      val cantidadFilas = status(result)

      val estadoEsperado = Db.runWithAwait(estados.filter(x => x.dni === 432 && x.estado === RECHAZO_LOGISTICA).result.head)
      assert(estadoEsperado.recuperable == false)
      status(result) mustBe OK

    }
  }

}