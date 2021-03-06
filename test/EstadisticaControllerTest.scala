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
        Venta(432, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
        Venta(437, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
        Venta(438, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
      )
      val estadosEsperados = Seq(
        Estado("200", 1, CREADO, DateTime.now),
        Estado("200", 1, VALIDADO, DateTime.now),
        Estado("200", 1, AUDITORIA_APROBADA, DateTime.now),
        Estado("200", 1, VISITA_CREADA, DateTime.now),
        Estado("200", 1, VISITA_CONFIRMADA, DateTime.now),

        Estado("200", 2, CREADO, DateTime.now),
        Estado("200", 2, VALIDADO, DateTime.now),
        Estado("200", 2, AUDITORIA_APROBADA, DateTime.now),
        Estado("200", 2, VISITA_CREADA, DateTime.now),
        Estado("200", 2, RECHAZO_LOGISTICA, DateTime.now),

        Estado("200", 3, CREADO, DateTime.now, true),
        Estado("200", 3, VALIDADO, DateTime.now),
        Estado("200", 3, AUDITORIA_APROBADA, DateTime.now),

        Estado("200", 4, CREADO, DateTime.now, true, Some("cantidad impagos 25")),
        Estado("200", 5, CREADO, DateTime.now, true, Some("meses de traspaso 2")),
      )

      val validacionesEsperadas = Seq(
        Validacion(1, true, true, true, None, None, None),
        Validacion(2, true, true, true, None, None, None),
        Validacion(3, true, true, true, None, None, None),
      )

      val auditoriasEsperadas = Seq(
        Auditoria(1, 1, "a", "ds0"),
        Auditoria(1, 2, "a", "ds0"),
        Auditoria(1, 3, "a", "ds0"),
      )

      val visitasEsperadas = Seq(
        Visita(1, 1, "2", "2", "3", "2", None, DateTime.now, "a", "creada", None),
        Visita(1, 2, "2", "2", "3", "2", None, DateTime.now, "a", "creada", None),
      )


      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)
      Db.runWithAwait(validaciones ++= validacionesEsperadas)
      Db.runWithAwait(visitas ++= visitasEsperadas)
      Db.runWithAwait(auditorias ++= auditoriasEsperadas)


      /*val Some(result) = route(app, FakeRequest(GET, "/recuperarVenta/all").withHeaders("My-Authorization" -> Token.header))
      val cantidadFilas = contentAsJson(result).asInstanceOf[JsArray].value.length

      assert(cantidadFilas == 3)
      status(result) mustBe OK*/

    }

    "estadistica visitas" in {
      Db.inicializarDb

      val json = Json.parse(
        """
        {
          "fechaDesde": "2017-08-02T10:10:10",
          "fechaHasta": "2019-08-02T10:10:10",
          "fechaDesdeVisita": "2017-08-02T10:10:10",
          "fechaHastaVisita": "2019-08-02T10:10:10"
          }
        """)

      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
        Venta(437, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
        Venta(438, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
      )
      val estadosEsperados = Seq(
        Estado("200", 1, CREADO, DateTime.now),
        Estado("200", 1, VALIDADO, DateTime.now),
        Estado("200", 1, AUDITORIA_APROBADA, DateTime.now),
        Estado("200", 1, VISITA_CREADA, DateTime.now),
        Estado("200", 1, VISITA_CONFIRMADA, DateTime.now),

        Estado("200", 2, CREADO, DateTime.now),
        Estado("200", 2, VALIDADO, DateTime.now),
        Estado("200", 2, AUDITORIA_APROBADA, DateTime.now),
        Estado("200", 2, VISITA_CREADA, DateTime.now),
        Estado("200", 2, RECHAZO_LOGISTICA, DateTime.now),

        Estado("200", 3, CREADO, DateTime.now, true),
        Estado("200", 3, VALIDADO, DateTime.now),
        Estado("200", 3, AUDITORIA_APROBADA, DateTime.now),

        Estado("200", 4, CREADO, DateTime.now, true, Some("cantidad impagos 25")),
        Estado("200", 5, CREADO, DateTime.now, true, Some("meses de traspaso 2")),
      )

      val validacionesEsperadas = Seq(
        Validacion(1, true, true, true, None, None, None),
        Validacion(2, true, true, true, None, None, None),
        Validacion(3, true, true, true, None, None, None),
      )

      val auditoriasEsperadas = Seq(
        Auditoria(1,1, "a",  "ds0"),
        Auditoria(1,2, "a",  "ds0"),
        Auditoria(1,3, "a",  "ds0"),
      )

      val visitasEsperadas = Seq(
        Visita(1, 1, "2", "2", "3", "2", None, DateTime.now, "a", "creada", None),
        Visita(1, 2, "2", "2", "3", "2", None, DateTime.now, "a", "creada", None),
      )


      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)
      Db.runWithAwait(validaciones ++= validacionesEsperadas)
      Db.runWithAwait(visitas ++= visitasEsperadas)
      Db.runWithAwait(auditorias ++= auditoriasEsperadas)


      val Some(result) = route(app, FakeRequest(POST, "/estadistica/visitas").withJsonBody(json).withHeaders("My-Authorization" -> Token.header))
      val cantidadFilas = contentAsJson(result).asInstanceOf[JsArray]

      status(result) mustBe OK


    }

    "estadistica archivos" in {
      Db.inicializarDb

      val json = Json.parse(
        """
        {
          "fechaDesde": "2017-08-02T10:10:10",
          "fechaHasta": "2019-08-02T10:10:10"
          }
        """)

      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
        Venta(437, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
        Venta(438, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
      )
      val estadosEsperados = Seq(
        Estado("200", 1, CREADO, DateTime.now),
        Estado("200", 1, VALIDADO, DateTime.now),
        Estado("200", 1, AUDITORIA_APROBADA, DateTime.now),
        Estado("200", 1, VISITA_CREADA, DateTime.now),
        Estado("200", 1, VISITA_CONFIRMADA, DateTime.now),

        Estado("200", 2, CREADO, DateTime.now),
        Estado("200", 2, VALIDADO, DateTime.now),
        Estado("200", 2, AUDITORIA_APROBADA, DateTime.now),
        Estado("200", 2, VISITA_CREADA, DateTime.now),
        Estado("200", 2, RECHAZO_LOGISTICA, DateTime.now),

        Estado("200", 3, CREADO, DateTime.now, true),
        Estado("200", 3, VALIDADO, DateTime.now),
        Estado("200", 3, AUDITORIA_APROBADA, DateTime.now),

        Estado("200", 4, CREADO, DateTime.now, true, Some("cantidad impagos 25")),
        Estado("200", 5, CREADO, DateTime.now, true, Some("meses de traspaso 2")),
      )

      val validacionesEsperadas = Seq(
        Validacion(1, true, true, true, None, None, None),
        Validacion(2, true, true, true, None, None, None),
        Validacion(3, true, true, true, None, None, None),
      )

      val auditoriasEsperadas = Seq(
        Auditoria(1, 1, "CLAUDIO MANURP-AM-OK-1.mp3",  "ds0"),
        Auditoria(1, 2, "CLAUDIO MANURP-AM-OK-1.mp3",  "ds0"),
        Auditoria(1, 3, "CLAUDIO MANURP-AM-OK-1.mp3",  "ds0"),
      )

      val visitasEsperadas = Seq(
        Visita(1, 1, "2", "2", "3", "2", None, DateTime.now, "a", "creada", None),
        Visita(1, 2, "2", "2", "3", "2", None, DateTime.now, "a", "creada", None),
      )


      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)
      Db.runWithAwait(validaciones ++= validacionesEsperadas)
      Db.runWithAwait(visitas ++= visitasEsperadas)
      Db.runWithAwait(auditorias ++= auditoriasEsperadas)


      val Some(result) = route(app, FakeRequest(POST, "/estadistica/archivos").withJsonBody(json).withHeaders("My-Authorization" -> Token.header))
      val cantidadFilas = contentAsJson(result).asInstanceOf[JsArray].value.length

      status(result) mustBe OK
      assert(cantidadFilas === 3)

    }
  }
}