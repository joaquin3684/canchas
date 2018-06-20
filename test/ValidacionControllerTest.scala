import akka.http.scaladsl.model.DateTime
import models.{Estado, Estados, Validacion, Venta}
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsArray, JsValue, Json}
import play.api.test.Helpers._
import play.api.test.FakeRequest
import repositories.Db
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas.{estados, validaciones, ventas}
import services.JsonMapper

/**
  * Unit tests can run without a full Play application.
  */
class ValidacionControllerTest extends PlaySpec with GuiceOneAppPerSuite with Estados {


  "ValidacionController" should {

    "ventas a validar" in {
      Db.inicializarDb

      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
      )
      val estadosEsperados = Seq(
        Estado("200", 432, CREADO, DateTime.now),
        Estado("200", 435, CREADO, DateTime.now),
        Estado("200", 436, CREADO, DateTime.now),
        Estado("200", 436, VALIDADO, DateTime.now),
        Estado("200", 435, RECHAZO_VALIDACION, DateTime.now),
      )
      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)


      val Some(result) = route(app, FakeRequest(GET, "/validacion/ventasAValidar").withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      val b = contentAsString(result)

      val jsonMapper = new JsonMapper
      val ventasObtenidas = jsonMapper.fromJson[Seq[Venta]](b)

      assert(ventasEsperadas.take(1) == ventasObtenidas)

    }

    "validar venta" in {

      // DATOS PARA INICIALIZAR TEST
      Db.inicializarDb
      val json = Json.parse(
        """
        {
          "codem": true,
          "superr": true,
          "afip": true,
          "motivoCodem": null,
          "motivoSuper": null,
          "motivoAfip": null,
          "capitas": 4,
          "dni": 432
          }
        """)
      val jsonMapper = new JsonMapper

      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
      )
      val estadosEsperados = Seq(
        Estado("200", 432, CREADO, DateTime.now),
        Estado("200", 435, CREADO, DateTime.now),
        Estado("200", 436, CREADO, DateTime.now)
      )
      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)

      // FIN DE DATOS PARA INICIALIZAR TEST


      val Some(result) = route(app, FakeRequest(POST, "/validacion/validar").withJsonBody(json).withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      val bodyText = contentAsString(result)
      val estado = Db.runWithAwait(estados.filter( x => x.dni === 432 && x.estado === VALIDADO).result.head)
      val validacion = Db.runWithAwait(validaciones.filter(x => x.dni === 432).result.head)
      val estadoEsperado = Estado("200", 432, VALIDADO, DateTime.now, None, estado.id)
      val validacionEsperada = Validacion(432, true, true, true, 4, None, None, None)


      status(result) mustEqual OK
      bodyText mustBe "validado"
      assert(estado == estadoEsperado)
      assert(validacion == validacionEsperada)

    }

    "traer todas las ventas validadas" in {

      // DATOS PARA INICIALIZAR TEST
      Db.inicializarDb

      val jsonMapper = new JsonMapper

      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
      )
      val estadosEsperados = Seq(
        Estado("200", 432, VALIDADO, DateTime.now),
        Estado("200", 435, VALIDADO, DateTime.now),
        Estado("200", 436, CREADO, DateTime.now)
      )
      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)

      // FIN DE DATOS PARA INICIALIZAR TEST

      val Some(result) = route(app, FakeRequest(GET, "/validacion/all").withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      val json = contentAsJson(result).toString
      val ventasObtenidas = jsonMapper.fromJson[Seq[Venta]](json)

      status(result) mustBe OK
      assert(ventasObtenidas == ventasEsperadas.take(2))
    }

    "modificar venta" in {

      // DATOS PARA INICIALIZAR TEST
      Db.inicializarDb
      val json = Json.parse(
        """
        {
          "dni": 1234,
          "nombre": "1",
          "nacionalidad": "1",
          "domicilio": "1",
          "localidad": "1",
          "telefono": "1",
          "cuil": "1",
          "estadoCivil": "1",
          "edad": 1,
          "idObraSocial": "cobertec",
          "piso": null,
          "dpto": null,
          "fechaCreacion": "2017-02-03T20:20:00",
          "fechaNacimiento": "2017-02-03T20:20:00",
          "celular": null,
          "horaContactoTel": "1",
          "horaContactoCel": null,
          "zona": "1",
          "codigoPostal": 1,
          "base": null,
          "user":"300"
          }
        """)
      val jsonMapper = new JsonMapper

      val ventasEsperadas = Seq(
        Venta(467, "marcela Jordan", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "Sur", 47, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
      )
      val estadosEsperados = Seq(
        Estado("200", 467, CREADO, DateTime.now),
        Estado("200", 435, CREADO, DateTime.now),
        Estado("200", 436, CREADO, DateTime.now)
      )
      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)

      // FIN DE DATOS PARA INICIALIZAR TEST

      val Some(result) = route(app, FakeRequest(PUT, "/validacion/updateVenta/467").withJsonBody(json).withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      val bodyText = status(result)

      val rootNode = jsonMapper.getJsonNode(json.toString)
      val user = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(rootNode, "user")
      val f = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(rootNode, "fechaCreacion")
      val fechaCreacion = DateTime.fromIsoDateTimeString(f).get
      val ventaEsperada = jsonMapper.fromJson[Venta](rootNode.toString)
      val venta = Db.runWithAwait(ventas.filter(_.dni === 1234).result.head)

      val estado = Db.runWithAwait(estados.filter( e => e.dni === 1234 && e.estado === CREADO).result.head)

      val estadoEsperado = Estado(user, ventaEsperada.dni, CREADO, fechaCreacion, None, estado.id)

      assert(estadoEsperado == estado)
      assert(ventaEsperada == venta)

    }


  }
}