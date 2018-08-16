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
      Usuario("NVALSI", "NAHIR2301", "NAHIR2301".bcrypt, "200"),
      Usuario("JPJORDAN", "JPABLO123", "JPABLO123".bcrypt, "200"),
      Usuario("PCANADELL", "PABLOC1323", "PABLOC1323".bcrypt, "300"),
      Usuario("KUEGA", "400", "IARA0709".bcrypt, "400"),
      Usuario("LPEDERCINI", "VENDEDORA", "LETY1234".bcrypt, "VENDEDORA"),
      Usuario("OMCONSTANZA", "CADETE", "37766576".bcrypt, "CADETE"),
      Usuario("FJORDAN", "EXTERNO", "MARITA1323".bcrypt, "EXTERNO"),
      Usuario("SANDRA", "PROMOTORA", "CHUAVECHITO2018".bcrypt, "PROMOTORA"),
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
      Perfil("ESTADISTICAS"),
      Perfil("VENDEDORA"),
      Perfil("CADETE"),
      Perfil("EXTERNO"),
      Perfil("PROMOTORA")
    ),

    Schemas.usuariosPerfiles ++= Seq(
      UsuarioPerfil("NVALSI", "SUPERVISOR CALL"),
      UsuarioPerfil("KUEGA", "SUPERVISOR LOGISTICA"),
      UsuarioPerfil("LPEDERCINI", "ESTADISTICAS"),
      UsuarioPerfil("OMCONSTANZA", "ESTADISTICAS"),
      UsuarioPerfil("FJORDAN", "SUPERVISOR VENDEDORAS"),
      UsuarioPerfil("SANDRA", "SUPERVISOR VENDEDORAS"),
      UsuarioPerfil("JPJORDAN", "ADMINISTRADOR VENTAS"),
      UsuarioPerfil("PCANADELL", "ESTADISTICAS")
    ),


    Schemas.obrasSociales ++= Seq(
      ObraSocial("COBERTEC"),
      ObraSocial("MYC"),
      ObraSocial("CELIUS"),
      ObraSocial("PLUS"),
      ObraSocial("SANATORIAL"),
      ObraSocial("SABER"),
      ObraSocial("OSPATRONES"),
    ),

    Schemas.usuariosObrasSociales ++= Seq(
      UsuarioObraSocial("NVALSI", "COBERTEC"),
      UsuarioObraSocial("NVALSI", "MYC"),
      UsuarioObraSocial("NVALSI", "CELIUS"),
      UsuarioObraSocial("NVALSI", "PLUS"),
      UsuarioObraSocial("NVALSI", "SANATORIAL"),
      UsuarioObraSocial("NVALSI", "SABER"),
      UsuarioObraSocial("NVALSI", "OSPATRONES"),
      UsuarioObraSocial("KUEGA", "COBERTEC"),
      UsuarioObraSocial("KUEGA", "MYC"),
      UsuarioObraSocial("KUEGA", "CELIUS"),
      UsuarioObraSocial("KUEGA", "PLUS"),
      UsuarioObraSocial("KUEGA", "SANATORIAL"),
      UsuarioObraSocial("KUEGA", "SABER"),
      UsuarioObraSocial("KUEGA", "OSPATRONES"),
      UsuarioObraSocial("LPEDERCINI", "COBERTEC"),
      UsuarioObraSocial("LPEDERCINI", "MYC"),
      UsuarioObraSocial("LPEDERCINI", "CELIUS"),
      UsuarioObraSocial("LPEDERCINI", "PLUS"),
      UsuarioObraSocial("LPEDERCINI", "SANATORIAL"),
      UsuarioObraSocial("LPEDERCINI", "SABER"),
      UsuarioObraSocial("LPEDERCINI", "OSPATRONES"),
      UsuarioObraSocial("OMCONSTANZA", "COBERTEC"),
      UsuarioObraSocial("OMCONSTANZA", "MYC"),
      UsuarioObraSocial("OMCONSTANZA", "CELIUS"),
      UsuarioObraSocial("OMCONSTANZA", "PLUS"),
      UsuarioObraSocial("OMCONSTANZA", "SANATORIAL"),
      UsuarioObraSocial("OMCONSTANZA", "SABER"),
      UsuarioObraSocial("OMCONSTANZA", "OSPATRONES"),
      UsuarioObraSocial("FJORDAN", "COBERTEC"),
      UsuarioObraSocial("FJORDAN", "MYC"),
      UsuarioObraSocial("FJORDAN", "CELIUS"),
      UsuarioObraSocial("FJORDAN", "PLUS"),
      UsuarioObraSocial("FJORDAN", "SANATORIAL"),
      UsuarioObraSocial("FJORDAN", "SABER"),
      UsuarioObraSocial("FJORDAN", "OSPATRONES"),
      UsuarioObraSocial("SANDRA", "COBERTEC"),
      UsuarioObraSocial("SANDRA", "MYC"),
      UsuarioObraSocial("SANDRA", "CELIUS"),
      UsuarioObraSocial("SANDRA", "PLUS"),
      UsuarioObraSocial("SANDRA", "SANATORIAL"),
      UsuarioObraSocial("SANDRA", "SABER"),
      UsuarioObraSocial("SANDRA", "OSPATRONES"),
      UsuarioObraSocial("JPJORDAN", "COBERTEC"),
      UsuarioObraSocial("JPJORDAN", "MYC"),
      UsuarioObraSocial("JPJORDAN", "CELIUS"),
      UsuarioObraSocial("JPJORDAN", "PLUS"),
      UsuarioObraSocial("JPJORDAN", "SANATORIAL"),
      UsuarioObraSocial("JPJORDAN", "SABER"),
      UsuarioObraSocial("JPJORDAN", "OSPATRONES"),
      UsuarioObraSocial("PCANADELL", "COBERTEC"),
      UsuarioObraSocial("PCANADELL", "MYC"),
      UsuarioObraSocial("PCANADELL", "CELIUS"),
      UsuarioObraSocial("PCANADELL", "PLUS"),
      UsuarioObraSocial("PCANADELL", "SANATORIAL"),
      UsuarioObraSocial("PCANADELL", "SABER"),
      UsuarioObraSocial("PCANADELL", "OSPATRONES"),
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
      PerfilPantalla("ESTADISTICAS", "estadistica"),
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
