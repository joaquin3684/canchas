import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsArray, JsValue, Json}
import play.api.test.Helpers._
import play.api.test.{FakeRequest}
import repositories.Db

import slick.jdbc.MySQLProfile.api._

import schemas.Schemas.{usuarios, usuariosObrasSociales, usuariosPerfiles}
/**
  * Unit tests can run without a full Play application.
  */
class UsuarioControllerTest extends PlaySpec with GuiceOneAppPerSuite {


  "UsuarioController" should {

     "crear un usuario con uno o mas perfiles asignados y una o mas obras sociales asignadas" in {
      Db.inicializarDb

      val json = Json.parse(
        """
        {
          "user": "600",
          "email": "600",
          "password": "400",
          "nombre": "600",
          "obrasSociales":[{"nombre":"cobertec"}],
          "perfiles":[{"nombre":"admin"}]
        }
        """)

      val cantPreviaUsObs = Db.runWithAwait(usuariosObrasSociales.length.result)
      val cantPreviaUs = Db.runWithAwait(usuarios.length.result)
      val cantPreviaPerf = Db.runWithAwait(usuariosPerfiles.length.result)

      val Some(result) = route(app, FakeRequest(POST, "/usuario/create").withJsonBody(json).withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      val b = contentAsString(result)
      val cantActualUsObs = Db.runWithAwait(usuariosObrasSociales.length.result)
      val cantActualUs = Db.runWithAwait(usuarios.length.result)
      val cantActualPerf = Db.runWithAwait(usuariosPerfiles.length.result)

      assert(cantPreviaUs + 1 == cantActualUs)
      assert(cantPreviaUsObs + 1 == cantActualUsObs)
      assert(cantPreviaPerf + 1 == cantActualPerf)

    }

    "traerse todos los usuarios creados " in {
      Db.inicializarDb
      val Some(result) = route(app, FakeRequest(GET, "/usuario/all").withHeaders("My-Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjAwIiwib2JyYXNTb2NpYWxlcyI6WyJjb2JlcnRlYyIsIm1lZGljdXMiLCJvc2RlIl0sInBlcm1pc29zIjpbImF1ZGl0b3JpYSIsImxvZ2lzdGljYSIsInVzdWFyaW8iLCJ2YWxpZGFjaW9uIiwidmVudGEiXX0.IS_NWi36CSS5gVsV3kU6wSrLXfEV3B1tNb3moat6te0"))
      val cantRows = contentAsJson(result).asInstanceOf[JsArray].value.length

      assert(cantRows == 3)

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
          "obrasSociales":[{ "nombre": "cobertec" }, { "nombre" : "osde" }],
          "perfiles":[{ "nombre": "admin" }]
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