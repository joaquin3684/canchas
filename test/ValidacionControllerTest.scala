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
        Venta(432, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
      )
      val estadosEsperados = Seq(
        Estado("200", 1, CREADO, DateTime.now),
        Estado("200", 2, CREADO, DateTime.now),
        Estado("200", 3, CREADO, DateTime.now),
        Estado("200", 3, VALIDADO, DateTime.now),
        Estado("200", 2, RECHAZO_VALIDACION, DateTime.now),
      )
      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)


      val Some(result) = route(app, FakeRequest(GET, "/validacion/ventasAValidar").withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      val cantidadFilas = contentAsJson(result).asInstanceOf[JsArray].value.length


      assert(cantidadFilas == 1)

    }

    "validar venta" in {

      // DATOS PARA INICIALIZAR TEST
      Db.inicializarDb
      val json = Json.parse(
        """
        {
          "idVenta": 1,
          "codem": true,
          "superr": true,
          "afip": true,
          "motivoCodem": null,
          "motivoSuper": null,
          "motivoAfip": null,
          "datosEmpresa": {
            "empresa": "prueba",
            "direccion": "prueba",
            "localidad": "prueba",
            "cantidadEmpleados": "prueba",
            "horaEntrada": "prueba",
            "horaSalida": "prueba",
            "idVenta":1


          }
          }
        """)


      val jsonCodemFalse = Json.parse(
        """
        {
          "idVenta": 2,
          "codem": false,
          "superr": true,
          "afip": true,
          "motivoCodem": "hijo discapacitado",
          "motivoSuper": null,
          "motivoAfip": null,
                    "datosEmpresa": {
             "empresa": "prueba",
             "direccion": "prueba",
             "localidad": "prueba",
             "cantidadEmpleados": "prueba",
             "horaEntrada": "prueba",
             "horaSalida": "prueba",
             "idVenta":2


           }
          }
        """)

      val jsonSuperFalse = Json.parse(
        """
        {
          "idVenta": 3,
          "codem": true,
          "superr": false,
          "afip": true,
          "motivoCodem": null,
          "motivoSuper": "impagos 2",
          "motivoAfip": null,
                    "datosEmpresa": {
             "empresa": "prueba",
             "direccion": "prueba",
             "localidad": "prueba",
             "cantidadEmpleados": "prueba",
             "horaEntrada": "prueba",
             "horaSalida": "prueba",
             "idVenta":3


           }
          }
        """)

      val jsonMasDeUnoFalse = Json.parse(
        """
        {
          "idVenta": 4,
          "codem": false,
          "superr": false,
          "afip": true,
          "motivoCodem": "discapacitado",
          "motivoSuper": "pepe",
          "motivoAfip": null,
                    "datosEmpresa": {
             "empresa": "prueba",
             "direccion": "prueba",
             "localidad": "prueba",
             "cantidadEmpleados": "prueba",
             "horaEntrada": "prueba",
             "horaSalida": "prueba",
             "idVenta":4


           }
          }
        """)
      val jsonMapper = new JsonMapper

      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
        Venta(437, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None),
      )
      val estadosEsperados = Seq(
        Estado("200", 1, CREADO, DateTime.now),
        Estado("200", 2, CREADO, DateTime.now),
        Estado("200", 3, CREADO, DateTime.now),
        Estado("200", 4, CREADO, DateTime.now),
      )
      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)

      // FIN DE DATOS PARA INICIALIZAR TEST


      val Some(result) = route(app, FakeRequest(POST, "/validacion/validar").withJsonBody(json).withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      val bodyText = contentAsString(result)

      val Some(result2) = route(app, FakeRequest(POST, "/validacion/validar").withJsonBody(jsonCodemFalse).withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      val bodyText2 = contentAsString(result2)

      val Some(result3) = route(app, FakeRequest(POST, "/validacion/validar").withJsonBody(jsonSuperFalse).withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      val bodyText3 = contentAsString(result3)

      val Some(result4) = route(app, FakeRequest(POST, "/validacion/validar").withJsonBody(jsonMasDeUnoFalse).withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      val bodyText4 = contentAsString(result4)

      val estadoValidado = Db.runWithAwait(estados.filter( x => x.idVenta === 1.toLong && x.estado === VALIDADO).result.head)
      val estadoRechazoCodem = Db.runWithAwait(estados.filter( x => x.idVenta === 2.toLong && x.estado === RECHAZO_VALIDACION).result.head)
      val estadoRechazoSuper = Db.runWithAwait(estados.filter( x => x.idVenta === 3.toLong && x.estado === RECHAZO_VALIDACION).result.head)
      val estadoRechazoVarios = Db.runWithAwait(estados.filter( x => x.idVenta === 4.toLong && x.estado === RECHAZO_VALIDACION).result.head)


      val validacionValidada = Db.runWithAwait(validaciones.filter(x => x.idVenta === 1.toLong).result.head)
      val validacionRechazoCodem = Db.runWithAwait(validaciones.filter(x => x.idVenta === 2.toLong).result.head)
      val validacionRechazoSuper = Db.runWithAwait(validaciones.filter(x => x.idVenta === 3.toLong).result.head)
      val validacionRechazoVarios = Db.runWithAwait(validaciones.filter(x => x.idVenta === 4.toLong).result.head)


      val estadoEsperadoValidado = Estado("200", 1, VALIDADO, DateTime.now, false, None, estadoValidado.id)
      val estadoEsperadoRechazoCodem = Estado("200", 2, RECHAZO_VALIDACION, DateTime.now, false, Some("hijo discapacitado"), estadoRechazoCodem.id)
      val estadoEsperadoRechazoSuper = Estado("200", 3, RECHAZO_VALIDACION, DateTime.now, true, Some("impagos 2"), estadoRechazoSuper.id)
      val estadoEsperadoRechazoVarios = Estado("200", 4, RECHAZO_VALIDACION, DateTime.now, false, Some("discapacitado + pepe"), estadoRechazoVarios.id)

      val validacionEsperadaValidada = Validacion(1, true, true, true, None, None, None)
      val validacionEsperadaRechazoCodem = Validacion(2, false, true, true, Some("hijo discapacitado"), None, None)
      val validacionEsperadaRechazoSuper = Validacion(3, true, false, true, None, Some("impagos 2"), None)
      val validacionEsperadaRechazoVarios = Validacion(4, false, false, true, Some("discapacitado"), Some("pepe"), None)


      status(result) mustEqual OK
      assert(estadoValidado == estadoEsperadoValidado)
      assert(estadoRechazoCodem == estadoEsperadoRechazoCodem)
      assert(estadoRechazoSuper == estadoEsperadoRechazoSuper)
      assert(estadoRechazoVarios == estadoEsperadoRechazoVarios)
      assert(validacionValidada == validacionEsperadaValidada)
      assert(validacionRechazoCodem == validacionEsperadaRechazoCodem)
      assert(validacionRechazoSuper == validacionEsperadaRechazoSuper)
      assert(validacionRechazoVarios == validacionEsperadaRechazoVarios)

    }

    "traer todas las ventas validadas" in {

      // DATOS PARA INICIALIZAR TEST
      Db.inicializarDb

      val jsonMapper = new JsonMapper

      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None, None, None, None, 1),
        Venta(435, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None, None, None, None, 2),
        Venta(436, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None, None, None, None, 3),
      )
      val estadosEsperados = Seq(
        Estado("200", 1, VALIDADO, DateTime.now),
        Estado("200", 2, VALIDADO, DateTime.now),
        Estado("200", 3, CREADO, DateTime.now)
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
        Venta(467, "marcela Jordan", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "Sur", 47, "20hs", None, None, None, None, None, None, None, None, 1),
        Venta(435, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None, None, None, None, 2),
        Venta(436, "pepe", "argentina", "tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "COBERTEC", Some(DateTime.now), "sur", 45, "20hs", None, None, None, None, None, None, None, None, 3),
      )
      val estadosEsperados = Seq(
        Estado("200", 1, CREADO, DateTime.now),
        Estado("200", 2, CREADO, DateTime.now),
        Estado("200", 3, CREADO, DateTime.now)
      )
      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)

      // FIN DE DATOS PARA INICIALIZAR TEST

      val Some(result) = route(app, FakeRequest(PUT, "/validacion/updateVenta/1").withJsonBody(json).withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      val bodyText = status(result)

      val rootNode = jsonMapper.getJsonNode(json.toString)
      val user = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(rootNode, "user")
      val f = jsonMapper.getAndRemoveElementAndRemoveExtraQuotes(rootNode, "fechaCreacion")
      val fechaCreacion = DateTime.fromIsoDateTimeString(f).get
      val ventaEsperada = jsonMapper.fromJson[Venta](rootNode.toString)
      val venta = Db.runWithAwait(ventas.filter(_.id === 1.toLong).result.head)

      val estado = Db.runWithAwait(estados.filter( e => e.idVenta === 1.toLong && e.estado === CREADO).result.head)

      val estadoEsperado = Estado(user, 1, CREADO, fechaCreacion, false, None, estado.id)

      assert(estadoEsperado == estado)
      assert(ventaEsperada.copy(id = 1) == venta)

    }


  }
}