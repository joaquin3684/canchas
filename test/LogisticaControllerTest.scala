import akka.http.scaladsl.model.DateTime
import models._
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsArray, JsValue, Json}
import play.api.test.Helpers._
import play.api.test.FakeRequest
import repositories.Db
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas.{estados, visitas, ventas}
import services.JsonMapper

/**
  * Unit tests can run without a full Play application.
  */
class LogisticaControllerTest extends PlaySpec with GuiceOneAppPerSuite with Estados {


  "LogisticaController" should {

    "ventas sin visitas" in {
      Db.inicializarDb

      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(437, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(438, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
      )
      val estadosEsperados = Seq(
        Estado("200", 432, AUDITORIA_APROBADA, DateTime.now),
        Estado("200", 435, AUDITORIA_APROBADA, DateTime.now),
        Estado("200", 436, VISITA_CREADA, DateTime.now),
        Estado("200", 438, VALIDADO, DateTime.now),
        Estado("200", 437, RECHAZO_VALIDACION, DateTime.now),
      )

      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)

      val Some(result) = route(app, FakeRequest(GET, "/logistica/ventasSinVisita").withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      val cantidadFilas = contentAsJson(result).asInstanceOf[JsArray].value.length

      assert(cantidadFilas == 2)
      status(result) mustBe OK
    }

    "ventas a trabajar para generar las visitas" in {
      Db.inicializarDb

      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(437, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(438, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
      )
      val estadosEsperados = Seq(
        Estado("200", 432, AUDITORIA_APROBADA, DateTime.now),
        Estado("200", 435, AUDITORIA_APROBADA, DateTime.now),
        Estado("200", 436, VISITA_CREADA, DateTime.now),
        Estado("200", 438, VALIDADO, DateTime.now),
        Estado("200", 437, RECHAZO_VALIDACION, DateTime.now),
      )

      val reg = ".{19}".r
      val fechaMañana = org.joda.time.DateTime.now().plusDays(1).toDateTimeISO.toString()
      val f = reg.findFirstIn(fechaMañana)
      val visitasEsperadas = Seq(
        Visita(1, 436, "200", "1", "2", "3", "4", None, DateTime.fromIsoDateTimeString(f.get).get, "asd", "asd"),
      )
      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)
      Db.runWithAwait(visitas ++= visitasEsperadas)


      val Some(result) = route(app, FakeRequest(GET, "/logistica/ventasATrabajar").withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      val cantidadFilas = contentAsJson(result).asInstanceOf[JsArray].value.length

      assert(cantidadFilas == 3)
      status(result) mustBe OK

    }

    "generar visita" in {

      Db.inicializarDb
      val json = Json.parse(
        """
        {
          "dni": 432,
          "lugar": "200",
          "direccion": "200",
          "entreCalles": "200",
          "localidad": "200",
          "observacion": "200",
          "fecha": "2017-02-03T20:21:20",
          "hora": "200"
        }
        """)

      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(437, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(438, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
      )
      val estadosEsperados = Seq(
        Estado("200", 432, AUDITORIA_APROBADA, DateTime.now),
        Estado("200", 435, AUDITORIA_APROBADA, DateTime.now),
        Estado("200", 436, VISITA_CREADA, DateTime.now),
        Estado("200", 438, VALIDADO, DateTime.now),
        Estado("200", 437, RECHAZO_VALIDACION, DateTime.now),
      )

      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)

      val Some(result) = route(app, FakeRequest(POST, "/logistica/generarVisita").withJsonBody(json).withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      val bodyText = contentAsString(result)
      val visitaObtenida = Db.runWithAwait(visitas.filter(_.dni === 432).result.head)
      val estadoObtenido = Db.runWithAwait(estados.filter( e => e.dni === 432 && e.estado === VISITA_CREADA).result.head)

      val jsonMapper = new JsonMapper

      val node = jsonMapper.getJsonNode(json.toString())
      jsonMapper.putElement(node, "user", "200")
      jsonMapper.putElement(node, "estado", VISITA_CREADA)

      val visitaEsperada = jsonMapper.fromJson[Visita](node.toString).copy(id = visitaObtenida.id)
      val estadoEsperado = Estado("200", 432, VISITA_CREADA, DateTime.now, false, None, estadoObtenido.id)

      assert(estadoEsperado == estadoObtenido)
      assert(visitaObtenida == visitaEsperada)
    }

    "confirmar visita" in {

      Db.inicializarDb
      val json = Json.parse(
        """
        {
          "dni": 432,
          "idVisita": 1,
          "user": "200"
        }
        """)

      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(437, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(438, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
      )
      val estadosEsperados = Seq(
        Estado("200", 432, AUDITORIA_APROBADA, DateTime.now),
        Estado("200", 435, AUDITORIA_APROBADA, DateTime.now),
        Estado("200", 436, VISITA_CREADA, DateTime.now),
        Estado("200", 438, VALIDADO, DateTime.now),
        Estado("200", 437, RECHAZO_VALIDACION, DateTime.now),
      )

      val visitasEsperadas = Seq(
        Visita(1, 432, "200", "2", "2", "2", "2", None, DateTime.now, "20","a"),
      )


      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)
      Db.runWithAwait(visitas ++= visitasEsperadas)

      val Some(result) = route(app, FakeRequest(POST, "/logistica/confirmarVisita").withJsonBody(json).withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      val bodyText = contentAsString(result)

      val estadoNuevo = Db.runWithAwait(estados.filter( e => e.dni === 432 && e.estado === VISITA_CONFIRMADA).result.head)
      val visitaObtenida = Db.runWithAwait(visitas.filter(_.id === 1.toLong).result.head)
      val estadoEsperado = Estado("200", 432, VISITA_CONFIRMADA, DateTime.now, false, None, estadoNuevo.id)


      assert(estadoEsperado == estadoNuevo)
      assert(visitaObtenida == visitasEsperadas.take(1).head.copy(estado = VISITA_CONFIRMADA))
    }

    "repactar visita" in {

      Db.inicializarDb
      val json = Json.parse(
        """
        {
          "dni": 432,
          "lugar": "200",
          "direccion": "200",
          "entreCalles": "200",
          "localidad": "200",
          "observacion": "200",
          "fecha": "2017-02-03T20:21:20",
          "hora": "200"
        }
        """)

      val jsonMapper = new JsonMapper
      val node = jsonMapper.getJsonNode(json.toString())
      jsonMapper.putElement(node, "user", "200")
      jsonMapper.putElement(node, "estado", VISITA_REPACTADA)

      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(437, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(438, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
      )
      val estadosEsperados = Seq(
        Estado("200", 432, AUDITORIA_APROBADA, DateTime.now),
        Estado("200", 435, AUDITORIA_APROBADA, DateTime.now),
        Estado("200", 436, VISITA_CREADA, DateTime.now),
        Estado("200", 438, VALIDADO, DateTime.now),
        Estado("200", 437, RECHAZO_VALIDACION, DateTime.now),
      )

      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)

      val Some(result) = route(app, FakeRequest(POST, "/logistica/repactarVisita").withJsonBody(json).withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      val bodyText = contentAsString(result)

      val visitaObtenida = Db.runWithAwait(visitas.filter(x => x.dni === 432 && x.estado === VISITA_REPACTADA).result.head)
      val estadoObtenido = Db.runWithAwait(estados.filter(e => e.dni === 432 && e.estado === VISITA_REPACTADA).result.head)

      val visitaEsperada = jsonMapper.fromJson[Visita](node.toString).copy(id = visitaObtenida.id)
      val estadoEsperado = Estado("200", 432, VISITA_REPACTADA, DateTime.now, false, None, estadoObtenido.id)

      assert(visitaObtenida == visitaEsperada)
      assert(estadoObtenido == estadoEsperado)
    }

    "rechazar visita" in {

      Db.inicializarDb
      val json = Json.parse(
        """
        {
          "dni": 432,
          "observacion": "200",
          "recuperable": true

        }
        """)

      val jsonMapper = new JsonMapper
      val node = jsonMapper.getJsonNode(json.toString())
      val recuperable = jsonMapper.getAndRemoveElement(node, "recuperable").toBoolean

      val ventasEsperadas = Seq(
        Venta(432, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(435, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(436, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(437, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
        Venta(438, "pepe", "argentina","tres arroyos", "floresta", "4672-7473", "30-20123-02", "casada", 60, "osde", DateTime.now, "sur", 45, "20hs", None, None, None, None, None),
      )
      val estadosEsperados = Seq(
        Estado("200", 432, AUDITORIA_APROBADA, DateTime.now),
        Estado("200", 435, AUDITORIA_APROBADA, DateTime.now),
        Estado("200", 436, VISITA_CREADA, DateTime.now),
        Estado("200", 438, VALIDADO, DateTime.now),
        Estado("200", 437, RECHAZO_VALIDACION, DateTime.now),
      )

      Db.runWithAwait(ventas ++= ventasEsperadas)
      Db.runWithAwait(estados ++= estadosEsperados)

      val Some(result) = route(app, FakeRequest(POST, "/logistica/rechazar").withJsonBody(json).withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      val bodyText = contentAsString(result)

      val estadoObtenido = Db.runWithAwait(estados.filter( e => e.dni === 432 && e.estado === RECHAZO_LOGISTICA).result.head)
      val estadoEsperado = Estado("200", 432, RECHAZO_LOGISTICA, DateTime.now, recuperable, Some("200"), estadoObtenido.id)

      assert(estadoObtenido == estadoEsperado)
    }

    "traer todas las ventas con visitas generadas" in {

    }
  }
}