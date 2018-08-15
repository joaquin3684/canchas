package repositories


import models._
import schemas.Schemas
import slick.jdbc.MySQLProfile.api._
import com.github.t3hnar.bcrypt._
import slick.dbio.{DBIOAction, NoStream}
import slick.jdbc.meta.MTable

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global

object Db {

  val db = Database.forConfig("db.default")

  val initDb = DBIO.seq(

    sqlu"SET FOREIGN_KEY_CHECKS = 0",
    Schemas.allSchemas.truncate,
    sqlu"SET FOREIGN_KEY_CHECKS = 1",
    Schemas.usuarios ++= Seq(
      Usuario("200", "200", "200".bcrypt, "200", None),
      Usuario("300", "300", "300".bcrypt, "300", None),
      Usuario("400", "400", "400".bcrypt, "400", None),
      Usuario("VENDEDORA", "VENDEDORA", "VENDEDORA".bcrypt, "VENDEDORA", None),
      Usuario("CADETE", "CADETE", "CADETE".bcrypt, "CADETE", None),
      Usuario("EXTERNO", "EXTERNO", "EXTERNO".bcrypt, "EXTERNO", None),
      Usuario("PROMOTORA", "PROMOTORA", "PROMOTORA".bcrypt, "PROMOTORA", None),
    ),
    Schemas.perfiles ++= Seq(
      Perfil("ADMIN"),
      Perfil("OPERADOR VENTA"),
      Perfil("OPERADOR AUDITORIA"),
      Perfil("OPERADOR LOGISTICA"),
      Perfil("OPERADOR VALIDACION"),
      Perfil("SUPERVISOR CALL"),
      Perfil("SUPERVISOR VENDEDORAS"),
      Perfil("ADMINISTRADOR VENTAS"),
      Perfil("SUPERVISOR LOGISTICA"),
      Perfil("VENDEDORA"),
      Perfil("CADETE"),
      Perfil("EXTERNO"),
      Perfil("PROMOTORA")
    ),

    Schemas.usuariosPerfiles ++= Seq(
      UsuarioPerfil("200", "ADMIN"),
      UsuarioPerfil("300", "ADMIN"),
      UsuarioPerfil("400", "OPERADOR VENTA"),
      UsuarioPerfil("VENDEDORA", "VENDEDORA"),
      UsuarioPerfil("CADETE", "CADETE"),
      UsuarioPerfil("EXTERNO", "EXTERNO"),
      UsuarioPerfil("PROMOTORA", "PROMOTORA")
    ),


    Schemas.obrasSociales ++= Seq(
      ObraSocial("COBERTEC"),
      ObraSocial("OSDE"),
      ObraSocial("MEDICUS"),
    ),

    Schemas.usuariosObrasSociales ++= Seq(
      UsuarioObraSocial("200", "COBERTEC"),
      UsuarioObraSocial("200", "MEDICUS"),
      UsuarioObraSocial("200", "OSDE"),
      UsuarioObraSocial("300", "OSDE"),
      UsuarioObraSocial("300", "MEDICUS"),
      UsuarioObraSocial("400", "MEDICUS"),
      UsuarioObraSocial("400", "OSDE"),
      UsuarioObraSocial("400", "COBERTEC"),
    ),

    Schemas.pantallas ++= Seq(
      Pantalla("usuario"),
      Pantalla("venta"),
      Pantalla("validacion"),
      Pantalla("logistica"),
      Pantalla("logisticaOper"),
      Pantalla("auditoria"),
      Pantalla("administracionVenta"),
      Pantalla("recuperarVenta"),
      Pantalla("estadistica")
    ),

    Schemas.perfilesPantallas ++= Seq(
      PerfilPantalla("ADMIN", "usuario"),
      PerfilPantalla("ADMIN", "venta"),
      PerfilPantalla("ADMIN", "validacion"),
      PerfilPantalla("ADMIN", "logistica"),
      PerfilPantalla("ADMIN", "auditoria"),
      PerfilPantalla("ADMIN", "administracionVenta"),
      PerfilPantalla("ADMIN", "recuperarVenta"),
      PerfilPantalla("ADMIN", "estadistica"),
      PerfilPantalla("ADMIN", "logisticaOper"),
      PerfilPantalla("OPERADOR LOGISTICA", "logisticaOper"),
      PerfilPantalla("OPERADOR AUDITORIA", "auditoria"),
      PerfilPantalla("OPERADOR VALIDACION", "validacion"),
      PerfilPantalla("OPERADOR VENTA", "venta"),
      PerfilPantalla("OPERADOR VENTA", "recuperarVenta"),// solo la quedice recuperar la que dice enviar a call no
      PerfilPantalla("SUPERVISOR CALL", "usuario"),
      PerfilPantalla("SUPERVISOR CALL", "venta"),
      PerfilPantalla("SUPERVISOR CALL", "validacion"),
      PerfilPantalla("SUPERVISOR CALL", "logisticaOper"),
      PerfilPantalla("SUPERVISOR CALL", "auditoria"),
      PerfilPantalla("SUPERVISOR CALL", "recuperarVenta"),
      PerfilPantalla("SUPERVISOR CALL", "estadistica"),
      PerfilPantalla("SUPERVISOR VENDEDORAS", "venta"),
      PerfilPantalla("SUPERVISOR VENDEDORAS", "validacion"),
      PerfilPantalla("SUPERVISOR VENDEDORAS", "estadistica"),
      PerfilPantalla("SUPERVISOR VENDEDORAS", "usuario"),
      PerfilPantalla("ADMINISTRADOR VENTAS", "administracionVenta"),
      PerfilPantalla("ADMINISTRADOR VENTAS", "estadistica"),
      PerfilPantalla("ADMINISTRADOR VENTAS", "usuario"),
      PerfilPantalla("SUPERVISOR LOGISTICA", "logistica"),
      PerfilPantalla("SUPERVISOR LOGISTICA", "usuario"),
      PerfilPantalla("SUPERVISOR LOGISTICA", "estadistica"),


    ),

    Schemas.rutas ++= Seq(
      Ruta("/obraSocial/all"),
      Ruta("/perfil/all"),
      Ruta("/venta/all"),
      Ruta("/usuario/paraCreacion"),
      Ruta("/usuario/paraLogistica"),
    ),

    Schemas.pantallasRutas ++= Seq(
      PantallaRuta("usuario", "/obraSocial/all"),
      PantallaRuta("usuario", "/perfil/all"),
      PantallaRuta("venta", "/obraSocial/all"),
      PantallaRuta("venta", "/usuario/paraCreacion"),
      PantallaRuta("logistica", "/usuario/paraLogistica"),
    ),

  )


  def inicializarDb = {
    val j = db.run(MTable.getTables).flatMap(tables =>
    if(tables.isEmpty){
      db.run(schemas.allSchemas.create).andThen {
        case Success(_) => println("schema created")
      }
      } else {
      println("schema already created")
      Future.successful()
    }
    )
    Await.result(j, Duration.Inf)
    val e = Db.db.run(initDb.transactionally)
    Await.result(e, Duration.Inf)
  }



  val schemas = Schemas
  def runWithAwait[R](a: DBIOAction[R, NoStream, Nothing]) = {
   val b = db.run(a)
    Await.result(b, Duration.Inf)
  }

}
