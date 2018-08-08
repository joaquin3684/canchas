import akka.http.scaladsl.model.DateTime
import models.{Estado, Estados, Venta}
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsArray, JsValue, Json}
import play.api.test.Helpers._
import play.api.test.FakeRequest
import repositories.Db
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas.{Ventas, estados, ventas}
import services.JsonMapper
/**
  * Unit tests can run without a full Play application.
  */
class AuditoriaControllerTest extends PlaySpec with GuiceOneAppPerSuite with Estados{


  "AuditoriaController" should {

    "traerse todas las ventas para auditar" in {

      // DATOS INICIALES PARA TEST
      Db.inicializarDb

      val jsonMaper = new JsonMapper

      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None,None, None, None, 1),
        Venta(435, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None,None,None, None, 2),
        Venta(436, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None,None,None, None, 3),
      )
      val estadosEsperados = Seq(
        Estado("200", 1, CREADO, DateTime.now),
        Estado("200", 2, CREADO, DateTime.now),
        Estado("200", 1, VALIDADO, DateTime.now),
        Estado("200", 3, RECHAZO_VALIDACION, DateTime.now),
      )
      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)

      // FIN DE DATOS INICIALES PARA TEST

      val Some(result) = route(app, FakeRequest(GET, "/auditoria/ventasParaAuditar").withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      val cant = contentAsJson(result).asInstanceOf[JsArray].value.length
      assert(cant == 1)

    }

  "traerse todas las ventas auditadas" in {

        // DATOS INICIALES PARA TEST
        Db.inicializarDb

        val jsonMaper = new JsonMapper

        val ventasEsperadas = Seq(
          Venta(432, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None, None, None, None, 1),
          Venta(435, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None, None, None, None, 2),
          Venta(436, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None, None, None, None, 3),
        )
        val estadosEsperados = Seq(
          Estado("200", 1, CREADO, DateTime.now),
          Estado("200", 2, CREADO, DateTime.now),
          Estado("200", 1, AUDITORIA_APROBADA, DateTime.now),
          Estado("300", 3, AUDITORIA_APROBADA, DateTime.now),
        )
        Db.runWithAwait(ventas ++= ventasEsperadas)
        Db.runWithAwait(estados ++= estadosEsperados)

        // FIN DE DATOS INICIALES PARA TEST

        val Some(result) = route(app, FakeRequest(GET, "/auditoria/all").withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
        val jsonString = contentAsJson(result).toString()
        val ventasObtenidas = jsonMaper.fromJson[Seq[Venta]](jsonString)
        assert(ventasEsperadas.take(1) == ventasObtenidas)

      }
    "auditar venta" in {

      // DATOS INICIALES PARA TEST
      Db.inicializarDb

      val json = Json.parse(
        """
          {
          "idVenta": 1,
           "capitas": 1,
            "estado": "ok",
             "observacion": null,
              "cantidadAudios": 2,
               "empresa": null,
                "direccion": null,
                 "localidad": null,
                  "cantidadEmpleados": null,
                   "horaEntrada": null,
                    "horaSalida": null,
                     "recuperable": null
                      }
        """)

      val json2 = Json.parse(
        """
          {
          "idVenta": 3,
           "capitas": 1,
            "estado": "ok",
             "observacion": null,
              "cantidadAudios": 2,
               "empresa": "sd",
                "direccion": "sd",
                 "localidad": "sd",
                  "cantidadEmpleados": "sd",
                   "horaEntrada": "sd",
                    "horaSalida": "sd",
                     "recuperable": null
                      }
        """)

      val jsonMaper = new JsonMapper

      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None, None, None, None, 1),
        Venta(435, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None, None, None, None, 2),
        Venta(436, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None, None, None, None, 3),
      )
      val estadosEsperados = Seq(
        Estado("200", 1, CREADO, DateTime.now),
        Estado("200", 2, CREADO, DateTime.now),
        Estado("200", 2, AUDITORIA_APROBADA, DateTime.now),
        Estado("300", 3, CREADO, DateTime.now),
      )
      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)

      // FIN DE DATOS INICIALES PARA TEST

      val Some(result) = route(app, FakeRequest(POST, "/auditoria/auditar").withJsonBody(json).withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      status(result) mustBe OK

      val Some(result2) = route(app, FakeRequest(POST, "/auditoria/auditar").withJsonBody(json2).withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      status(result2) mustBe OK
    }

  }
}