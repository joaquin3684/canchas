package repositories

import akka.http.scaladsl.model.DateTime
import models._
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas.{estados, ventas}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

object VentaRepository extends Estados {


  def checkObraSocial(dni: Int)(implicit obs: Seq[String]): Future[Option[Venta]] =  {

    Db.db.run(ventas.filter( v => v.idObraSocial.inSetBind(obs) && v.dni === dni).result.headOption)

  }

  def create(venta: Venta, user: String, fecha: DateTime) = {
    val v = ventas += venta
    val estado = Estado(user, venta.dni, CREADO, fecha)
    val e = estados += estado
    val fullQuery = DBIO.seq(v, e)

    Db.db.run(fullQuery.transactionally)

  }

  def all(user: String)(implicit obs: Seq[String]) : Future[Seq[(Venta, Estado)]] = {
    val query = {
      for {
        e <- estados.filter( x => x.user === user && x.estado === CREADO)
        v <- ventas.filter(_.dni === e.dni)
      } yield (v,e)
    }
    Db.db.run(query.result)
  }

  def agregarEstado(estado: Estado) = {
    val e = estados += estado
    Db.db.run(e)
  }

  def modificarVenta(venta: Venta, dni:Int, user: String, fechaCreacion: DateTime) = {

    val deleteEstado = estados.filter(e => e.dni === dni && e.estado === CREADO).delete
    val updateV = ventas.filter(_.dni === dni).update(venta)
    val estadoModificado = Estado(user, venta.dni, CREADO, fechaCreacion)
    val updateEs = estados += estadoModificado
    val fullQuery = DBIO.seq(deleteEstado, updateV, updateEs)

    Db.db.run(fullQuery.transactionally)
  }
}
