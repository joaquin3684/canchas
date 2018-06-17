import akka.http.scaladsl.model.DateTime
import models.{Estado, Venta}
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
class VentaControllerTest extends PlaySpec with GuiceOneAppPerSuite {


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
          "idObraSocial": "osde",
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


      val Some(result) = route(app, FakeRequest(POST, "/venta/create").withJsonBody(json).withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      val b = contentAsString(result)
      val venta = Db.runWithAwait(ventas.filter(_.dni === 1234).result.head)
      val estado = Db.runWithAwait(estados.filter(_.dni === 1234).result.head)

      val jsonMaper = new JsonMapper
      val rootNode = jsonMaper.getJsonNode(json.toString)
      val user = jsonMaper.getAndRemoveElement(rootNode, "user")
      val f = jsonMaper.getAndRemoveElement(rootNode, "fechaCreacion")
      val fechaCreacion = DateTime.fromIsoDateTimeString(f).get
      val ventaEsperada = jsonMaper.fromJson[Venta](rootNode.toString)
      val estadoEsperado = Estado(user, ventaEsperada.dni, "Creado", fechaCreacion)

      assert(ventaEsperada == venta)
      assert(estadoEsperado == estado)
    }

    "traerse todas las ventas" in {
      Db.inicializarDb

      val jsonMaper = new JsonMapper

      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
     )
      val estadosEsperados = Seq(
        Estado("200", 432, "Creado", DateTime.now),
        Estado("200", 435, "Creado", DateTime.now),
        Estado("200", 436, "Creado", DateTime.now)
      )
      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)
      val Some(result) = route(app, FakeRequest(GET, "/venta/all").withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      val jsonString = contentAsJson(result).toString()
      val ventasObtenidas = jsonMaper.fromJson[Seq[Ventas]](jsonString)
      assert(ventasEsperadas == ventasObtenidas)

    }

    "traerse un usuario en especifico" in {
      Db.inicializarDb
      val Some(result) = route(app, FakeRequest(GET, "/usuario/get/200").withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      status(result) mustEqual OK

    }

    "modificar un usuario " in {

      Db.inicializarDb
      val json = Json.parse(
        """
        {
          "user": "500",
          "email": "500",
          "password": "200",
          "nombre": "700",
          "obrasSociales":[{"nombre":"cobertec"}, {"nombre":"osde"}],
          "perfiles":[{"nombre":"admin"}]
        }
        """)
      val Some(result) = route(app, FakeRequest(PUT, "/usuario/update/200").withJsonBody(json).withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      contentAsString(result) mustEqual "modificado"

    }

    "borrar un usuario" in {

      Db.inicializarDb
      val Some(result) = route(app, FakeRequest(DELETE, "/usuario/delete/200").withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      contentAsString(result) mustEqual "borrado"

    }

    "cambiar password propia" in {
      Db.inicializarDb
      val json = Json.parse(
        """
        {
          "password": "500"
        }
        """)

      val Some(result) = route(app, FakeRequest(POST, "/usuario/cambiarPasswordPropia").withJsonBody(json).withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      contentAsString(result) mustEqual "password modificada"
    }

    "cambiar password" in {
      Db.inicializarDb
      val json = Json.parse(
        """
        {
          "user": "200",
          "password": "500"
        }
        """)

      val Some(result) = route(app, FakeRequest(POST, "/usuario/cambiarPassword").withJsonBody(json).withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      contentAsString(result) mustEqual "password modificada"
    }
  }
}