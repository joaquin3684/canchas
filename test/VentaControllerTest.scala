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
class VentaControllerTest extends PlaySpec with GuiceOneAppPerSuite with Estados{


  "VentaController" should {

    "crear una venta" in {
      Db.inicializarDb

      val json = Json.parse(
        """
        {
          "dni": 1234,
          "nombre": "marcela jordan",
          "nacionalidad": "argentina",
          "domicilio": "tres arroyos",
          "localidad": "floresta",
          "telefono": "4672-7473",
          "cuil": "30-20123-02",
          "estadoCivil": "casada",
          "edad": 60,
          "idObraSocial": "COBERTEC",
          "piso": null,
          "dpto": null,
          "fechaCreacion": "2017-02-03T20:20:00",
          "fechaNacimiento": "2017-02-03T20:20:00",
          "celular": null,
          "horaContactoTel": "20hs",
          "horaContactoCel": null,
          "zona": "Sur",
          "codigoPostal": 47,
          "base": null,
          "user":"200"
          }
        """)


      val Some(result) = route(app, FakeRequest(POST, "/venta/create").withJsonBody(json).withHeaders("My-Authorization" -> Token.header))
      val b = contentAsString(result)
      val venta = Db.runWithAwait(ventas.filter(_.id === 1.toLong).result.head)
      val estado = Db.runWithAwait(estados.filter(_.idVenta === 1.toLong).result.head)

      val jsonMaper = new JsonMapper
      val rootNode = jsonMaper.getJsonNode(json.toString)
      val user = jsonMaper.getAndRemoveElementAndRemoveExtraQuotes(rootNode, "user")
      val f = jsonMaper.getAndRemoveElementAndRemoveExtraQuotes(rootNode, "fechaCreacion")
      val fechaCreacion = DateTime.fromIsoDateTimeString(f).get
      val ventaEsperada = jsonMaper.fromJson[Venta](rootNode.toString)
      val estadoEsperado = Estado(user, 1, "Creado", fechaCreacion, false, None, estado.id)

      assert(ventaEsperada.copy(id = 1) == venta)
      assert(estadoEsperado == estado)
    }

    "traerse todas las ventas" in {
      Db.inicializarDb

      val jsonMaper = new JsonMapper

      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
     )
      val estadosEsperados = Seq(
        Estado("200", 1, CREADO, DateTime.now),
        Estado("200", 2, CREADO, DateTime.now),
        Estado("200", 3, CREADO, DateTime.now),
      )
      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)
      val Some(result) = route(app, FakeRequest(GET, "/venta/all").withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      val cantidadFilas = contentAsJson(result).asInstanceOf[JsArray].value.length
      assert(cantidadFilas == 3)

    }

    "checkDniExistence" in {
      Db.inicializarDb
      val json = Json.parse(
        """
        {
          "dni": 432

          }
        """)

      val json2 = Json.parse(
        """
        {
          "dni": 467

          }
        """)
      val jsonMaper = new JsonMapper

      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
        Venta(432, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
        Venta(432, "pepe3", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC",Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
        Venta(434, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
      )
      val estadosEsperados = Seq(
        Estado("200", 1, CREADO, DateTime.now),
        Estado("200", 2, CREADO, DateTime.now),
        Estado("200", 3, CREADO, DateTime.now),
        Estado("200", 4, CREADO, DateTime.now),
      )
      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)
      val Some(result) = route(app, FakeRequest(POST, "/venta/existenciaDni").withJsonBody(json).withHeaders("My-Authorization" -> Token.header))

      status(result) mustEqual OK



      val Some(result2) = route(app, FakeRequest(POST, "/venta/existenciaDni").withJsonBody(json2).withHeaders("My-Authorization" -> Token.header))
      val texto = contentAsString(result2)

      assert(texto == "la venta no esta registrada")

    }
  }
}