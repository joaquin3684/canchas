package repositories

import akka.http.scaladsl.model.DateTime
import models._
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas.{ventas, estados}

import scala.concurrent.Future

class VentaRepository {
  val db = Database.forConfig("db.default")

  def create(venta: Venta, user: String) = {
    val v = ventas += venta
    val estado = Estado(user, venta.dni, "Creado", DateTime.now)
    val e = estados += estado
    val fullQuery = DBIO.seq(v, e)

    db.run(fullQuery.transactionally)

  }

  def all(user: String)(implicit obs: Seq[String]) : Future[Seq[Venta]] = {
    val query = {
      for {
        e <- estados.filter( x => x.user === user && x.estado === "Creado")
        v <- ventas.filter(_.dni === e.idVenta)
      } yield v
    }
    db.run(query.result)
  }

  def agregarEstado(estado: Estado) = {
    val e = estados += estado
    db.run(e)
  }

  def modificarVenta(venta: Venta, estado: Estado) = {
    val updateV = ventas.filter(_.dni === venta.dni).update(venta)
    val e = estados += estado
    val fullQuery = DBIO.seq(updateV, e)
    db.run(fullQuery.transactionally)
  }
}
