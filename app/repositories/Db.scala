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
      Usuario("vendedora", "vendedora", "vendedora".bcrypt, "vendedora", None),
      Usuario("cadete", "cadete", "cadete".bcrypt, "cadete", None),
      Usuario("externo", "externo", "externo".bcrypt, "externo", None),
      Usuario("promotora", "promotora", "promotora".bcrypt, "promotora", None),
    ),
    Schemas.perfiles ++= Seq(
      Perfil("admin"),
      Perfil("operador venta"),
      Perfil("operador auditoria"),
      Perfil("operador logistica"),
      Perfil("operador validacion"),
      Perfil("supervisor"),
      Perfil("vendedora"),
      Perfil("cadete"),
      Perfil("externo"),
      Perfil("promotora")
    ),

    Schemas.usuariosPerfiles ++= Seq(
      UsuarioPerfil("200", "admin"),
      UsuarioPerfil("300", "admin"),
      UsuarioPerfil("400", "operador venta"),
      UsuarioPerfil("vendedora", "vendedora"),
      UsuarioPerfil("cadete", "cadete"),
      UsuarioPerfil("externo", "externo"),
      UsuarioPerfil("promotora", "promotora")
    ),


    Schemas.obrasSociales ++= Seq(
      ObraSocial("cobertec"),
      ObraSocial("osde"),
      ObraSocial("medicus"),
    ),

    Schemas.usuariosObrasSociales ++= Seq(
      UsuarioObraSocial("200", "cobertec"),
      UsuarioObraSocial("200", "medicus"),
      UsuarioObraSocial("200", "osde"),
      UsuarioObraSocial("300", "osde"),
      UsuarioObraSocial("300", "medicus"),
      UsuarioObraSocial("400", "medicus"),
      UsuarioObraSocial("400", "osde"),
      UsuarioObraSocial("400", "cobertec"),
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

    Schemas.rutas ++= Seq(
      Ruta("/obraSocial/all"),
      Ruta("/perfil/all"),
      Ruta("/venta/all"),
      Ruta("/usuario/paraCreacion"),
      Ruta("/usuario/paraLogistica"),
    ),

    Schemas.perfilesPantallas ++= Seq(
      PerfilPantalla("admin", "usuario"),
      PerfilPantalla("admin", "venta"),
      PerfilPantalla("admin", "validacion"),
      PerfilPantalla("admin", "logistica"),
      PerfilPantalla("admin", "auditoria"),
      PerfilPantalla("admin", "administracionVenta"),
      PerfilPantalla("admin", "recuperarVenta"),
      PerfilPantalla("admin", "estadistica"),
      PerfilPantalla("admin", "logisticaOper"),
      PerfilPantalla("operador logistica", "logisticaOper"),
      PerfilPantalla("operador auditoria", "auditoria"),
      PerfilPantalla("operador venta", "venta"),
      PerfilPantalla("operador validacion", "validacion")

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
