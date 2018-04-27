package repositories

import akka.http.scaladsl.model.DateTime
import models._
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas.{ventas, estados}

import scala.concurrent.Future

class ValidacionRepository {

  val db = Database.forConfig("db.default")

  def validar(datos: (Option[Boolean], Option[Boolean], Option[Boolean], Option[String], Option[String], Option[String]), dni: Int, user:String) = {
    val ventaUpdated = ventas.filter(_.dni === dni).map( v => (v.codem, v.superr, v.afip, v.motivoCodem, v.motivoSupper, v.motivoAfip)).update(datos)
    val h = Seq(datos._1, datos._2, datos._3)
    val checkAprobacion = h.forall(_.get == true)
    val estado = if(checkAprobacion) Estado(user, dni, "Validado", DateTime.now) else Estado(user, dni, "Rechazo por validador", DateTime.now)
    val estadoNuevo = estados += estado
    val fullQuery = DBIO.seq(ventaUpdated, estadoNuevo)
    db.run(fullQuery.transactionally)
  }

  def checkObraSocial(dni: Int)(implicit obs: Seq[String]): Future[Option[Venta]] =  {

    db.run(ventas.filter( v => v.idObraSocial.inSetBind(obs) && v.dni === dni).result.headOption)

  }

  def all(user: String): Future[Seq[Venta]] = {
    val query = {
      for {
        e <- estados.filter( x => x.user === user && (x.estado === "Validado" || x.estado === "Rechazo por validador"))
        v <- ventas.filter(_.dni === e.idVenta)
      } yield v
    }
    db.run(query.result)

  }

  def ventasAValidar(user: String)(implicit obs: Seq[String]) : Future[Seq[Venta]] = {
    val query = {
      for {
        vali <- estados.filter(x => x.estado === "Validado" || x.estado === "Rechazo por validador").map(_.idVenta)
        e <- estados.filter(x => x.estado === "Creado" && x.idVenta =!= vali)
        v <- ventas.filter(x => x.dni === e.idVenta && x.idObraSocial.inSetBind(obs))
      } yield v
    }
    db.run(query.result)

  }


}
