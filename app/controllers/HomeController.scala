package controllers

import javax.inject._

import models._
import play.api.mvc._
import schemas.Schemas
import slick.jdbc.MySQLProfile.api._
import com.github.t3hnar.bcrypt._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    val db = Database.forConfig("db.default")


    /* val a = db.run(Schemas.allSchemas.drop)
     Await.result(a, Duration.Inf)
   */

     val initUser = Schemas.usuarios ++= Seq(
                                           Usuario("200", "200", "200".bcrypt, "200", None),
                                           Usuario("300", "300", "300".bcrypt, "300", None)
                                           )

     val initPerfiles = Schemas.perfiles ++= Seq(
                                               Perfil("admin"),
                                               Perfil("operador"),
                                               Perfil("supervisor")
                                               )

     val initUserPerfil = Schemas.usuariosPerfiles ++= Seq(
                                                         UsuarioPerfil("200", "admin"),
                                                         UsuarioPerfil("300", "admin")
                                                         )


     val initObrasSociales = Schemas.obrasSociales ++= Seq(
                                                     ObraSocial("cobertec"),
                                                     ObraSocial("osde"),
                                                     ObraSocial("medicus"),
     )

     val initUserObraSocial = Schemas.usuariosObrasSociales ++= Seq(
                                                                 UsuarioObraSocial("200", "cobertec"),
                                                                 UsuarioObraSocial("200", "medicus"),
                                                                 UsuarioObraSocial("200", "osde"),
                                                                 UsuarioObraSocial("300", "osde"),
                                                                 UsuarioObraSocial("300", "medicus"),
                                                                 )

     val initPantallas = Schemas.pantallas ++= Seq(
                                         Pantalla("usuario"),
                                         Pantalla("venta"),
                                         Pantalla("validacion"),
                                         )

     val initRutas = Schemas.rutas ++= Seq(
                                   Ruta("/obraSocial/all"),
                                   Ruta("/perfil/all"),
                                   )

     val initPerfilPantalla = Schemas.perfilesPantallas ++= Seq(
                                                               PerfilPantalla("admin", "usuario"),
                                                               PerfilPantalla("admin", "venta"),
                                                               PerfilPantalla("admin", "validacion"),

                                                               )

     val initPantallaRuta = Schemas.pantallasRutas ++= Seq(
                                                   PantallaRuta("usuario", "/obraSocial/all"),
                                                   PantallaRuta("usuario", "/perfil/all"),
                                                   PantallaRuta("venta", "/obraSocial/all"),
     )

     val seq = DBIO.seq(
                       Schemas.allSchemas.create,
                       initUser,
                       initObrasSociales,
                       initPerfiles,
                       initPantallas,
                       initRutas,
                       initUserObraSocial,
                       initUserPerfil,
                       initPerfilPantalla,
                       initPantallaRuta
     )
     val e = db.run(seq.transactionally)
     Await.result(e, Duration.Inf)
    Ok("base de datos creada")
  }

  def hola (nombre: String, apellido: String) = Action {
    Ok("")
  }

}
