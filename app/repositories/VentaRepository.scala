package repositories

import akka.http.scaladsl.model.DateTime
import models._
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas.{estados, ventas}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

object VentaRepository {


  def checkObraSocial(dni: Int)(implicit obs: Seq[String]): Future[Option[Venta]] =  {

    Db.db.run(ventas.filter( v => v.idObraSocial.inSetBind(obs) && v.dni === dni).result.headOption)

  }

  def create(venta: Venta, user: String) = {
    val v = ventas += venta
    val estado = Estado(user, venta.dni, "Creado", DateTime.now)
    val e = estados += estado
    val fullQuery = DBIO.seq(v, e)

    Db.db.run(fullQuery.transactionally)

  }

  def all(user: String)(implicit obs: Seq[String]) : Future[Seq[Venta]] = {
    val query = {
      for {
        e <- estados.filter( x => x.user === user && x.estado === "Creado")
        v <- ventas.filter(_.dni === e.dni)
      } yield v
    }
    Db.db.run(query.result)
  }

  def agregarEstado(estado: Estado) = {
    val e = estados += estado
    Db.db.run(e)
  }

  def modificarVenta(venta: Venta) = {

    val updateV = ventas.filter(_.dni === venta.dni).update(venta)
    Db.db.run(updateV)
  }
}
