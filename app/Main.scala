

import java.sql.Timestamp

import JsonFormats.{DateTimeDeserializer, DateTimeSerializer}
import akka.http.scaladsl.model.DateTime
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.module.SimpleModule
import models._
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.Success
import com.github.t3hnar.bcrypt._
import repositories.Db
import schemas.Schemas.{estados, usuariosObrasSociales, ventas, visitas}
import services.JsonMapper
import slick.jdbc.GetResult

object Main extends App {

  val db = Database.forConfig("db.default")


 /* val a = db.run(Schemas.allSchemas.drop)
  Await.result(a, Duration.Inf)
*/

 /* val initUser = Schemas.usuarios ++= Seq(
                                        Usuario("200", "200", "200".bcrypt, "200", None)
                                        )

  val initPerfiles = Schemas.perfiles ++= Seq(
                                            Perfil("admin"),
                                            Perfil("operador"),
                                            Perfil("supervisor")
                                            )

  val initUserPerfil = Schemas.usuariosPerfiles ++= Seq(
                                                      UsuarioPerfil("200", "admin")
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
                                                              )

  val initPantallas = Schemas.pantallas ++= Seq(
                                      Pantalla("usuario"),
                                      )

  val initRutas = Schemas.rutas ++= Seq(
                                Ruta("/obraSocial/all"),
                                Ruta("/perfil/all"),
                                )

  val initPerfilPantalla = Schemas.perfilesPantallas ++= Seq(
                                                            PerfilPantalla("admin", "usuario"),

                                                            )

  val initPantallaRuta = Schemas.pantallasRutas ++= Seq(
                                                    PantallaRuta("usuario", "/obraSocial/all"),
                                                    PantallaRuta("usuario", "/perfil/all"),
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
  val e = db.run(seq.transactionally)*/
 // Await.result(e, Duration.Inf)


  /*val query = {
    for {
      e <- estados.filter(x => x.estado === "Visita creada"  && !(x.idVenta in estados.filter(x => x.estado === "Visita confirmada").map(_.idVenta)))
      v <- ventas.filter(x => x.dni === e.idVenta && x.idObraSocial.inSetBind(obs))
      vis <- visitas.filter(x => x.idVenta === v.dni).sortBy(_.idVenta.desc)
    } yield (v, vis)
  }.result.statements.foreach(println)
*/

}


