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
      Usuario("300", "300", "300".bcrypt, "300", None)
    ),
    Schemas.perfiles ++= Seq(
      Perfil("admin"),
      Perfil("operador"),
      Perfil("supervisor")
    ),

    Schemas.usuariosPerfiles ++= Seq(
      UsuarioPerfil("200", "admin"),
      UsuarioPerfil("300", "admin")
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
    ),

    Schemas.pantallas ++= Seq(
      Pantalla("usuario"),
      Pantalla("venta"),
      Pantalla("validacion"),
      Pantalla("logistica"),
      Pantalla("auditoria"),
      Pantalla("administracionVenta")
    ),

    Schemas.rutas ++= Seq(
      Ruta("/obraSocial/all"),
      Ruta("/perfil/all"),
      Ruta("/venta/all"),
    ),

    Schemas.perfilesPantallas ++= Seq(
      PerfilPantalla("admin", "usuario"),
      PerfilPantalla("admin", "venta"),
      PerfilPantalla("admin", "validacion"),
      PerfilPantalla("admin", "logistica"),
      PerfilPantalla("admin", "auditoria"),
      PerfilPantalla("admin", "administracionVenta")
    ),

    Schemas.pantallasRutas ++= Seq(
      PantallaRuta("usuario", "/obraSocial/all"),
      PantallaRuta("usuario", "/perfil/all"),
      PantallaRuta("venta", "/obraSocial/all"),
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
