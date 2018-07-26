import akka.http.scaladsl.model.DateTime
import models._
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsArray, JsValue, Json}
import play.api.test.Helpers._
import play.api.test.FakeRequest
import repositories.Db
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas.{estados, auditorias, ventas, validaciones, visitas}
import services.JsonMapper

/**
  * Unit tests can run without a full Play application.
  */
class EstadisticaControllerTest extends PlaySpec with GuiceOneAppPerSuite with Estados {


  "EstadisticaController" should {

    "estadistica general" in {
      Db.inicializarDb

      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(437, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(438, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
      )
      val estadosEsperados = Seq(
        Estado("200", 432, CREADO, DateTime.now),
        Estado("200", 432, VALIDADO, DateTime.now),
        Estado("200", 432, AUDITORIA_APROBADA, DateTime.now),
        Estado("200", 432, VISITA_CREADA, DateTime.now),
        Estado("200", 432, VISITA_CONFIRMADA, DateTime.now),

        Estado("200", 435, CREADO, DateTime.now),
        Estado("200", 435, VALIDADO, DateTime.now),
        Estado("200", 435, AUDITORIA_APROBADA, DateTime.now),
        Estado("200", 435, VISITA_CREADA, DateTime.now),
        Estado("200", 435, RECHAZO_LOGISTICA, DateTime.now),

        Estado("200", 436, CREADO, DateTime.now, true),
        Estado("200", 436, VALIDADO, DateTime.now),
        Estado("200", 436, AUDITORIA_APROBADA, DateTime.now),

        Estado("200", 437, CREADO, DateTime.now, true, Some("cantidad impagos 25")),
        Estado("200", 438, CREADO, DateTime.now, true, Some("meses de traspaso 2")),
      )

      val validacionesEsperadas = Seq(
        Validacion(432, true, true, true, 4, None, None, None),
        Validacion(435, true, true, true, 4, None, None, None),
        Validacion(436, true, true, true, 4, None, None, None),
      )

      val visitasEsperadas = Seq(
        Visita(1, 432, "2", "2", "3", "2", None, DateTime.now, "a", "creada", None),
        Visita(1, 435, "2", "2", "3", "2", None, DateTime.now, "a", "creada", None),
      )


      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)
      Db.runWithAwait(validaciones ++= validacionesEsperadas)
      Db.runWithAwait(visitas ++= visitasEsperadas)


      /*val Some(result) = route(app, FakeRequest(GET, "/recuperarVenta/all").withHeaders("My-Authorization" -> Token.header))
      val cantidadFilas = contentAsJson(result).asInstanceOf[JsArray].value.length

      assert(cantidadFilas == 3)
      status(result) mustBe OK*/

    }

  }

}