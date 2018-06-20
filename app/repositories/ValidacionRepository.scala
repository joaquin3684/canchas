package repositories

import java.sql.Timestamp

import akka.http.scaladsl.model.DateTime
import models._
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas.{estados, validaciones, ventas}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

object ValidacionRepository extends Estados {

  implicit val localDateTimeMapping  = MappedColumnType.base[DateTime, Timestamp](
    dt => new Timestamp(dt.clicks),
    ts => DateTime(ts.getTime)
  )

  def checkObraSocial(dni: Int)(implicit obs: Seq[String]): Option[Venta] =  {

    val v = Db.db.run(ventas.filter( v => v.idObraSocial.inSetBind(obs) && v.dni === dni).result.headOption)
    Await.result(v, Duration.Inf)
  }

  def all(user: String): Future[Seq[Venta]] = {
    val query = {
      for {
        e <- estados.filter( x => x.user === user && (x.estado === VALIDADO || x.estado === RECHAZO_VALIDACION))
        v <- ventas.filter(_.dni === e.dni)
      } yield v
    }
    Db.db.run(query.result)

  }

  def ventasAValidar(user: String)(implicit obs: Seq[String]) : Future[Seq[(Venta, DateTime)]] = {
    val query = {
      for {
        e <- estados.filter(x => x.estado === CREADO && !(x.dni in estados.filter(x => x.estado === VALIDADO || x.estado === RECHAZO_VALIDACION).map(_.dni)))
        v <- ventas.filter(x => x.dni === e.dni && x.idObraSocial.inSetBind(obs))
      } yield (v, e.fecha)
    }
    Db.db.run(query.result)

  }

  def validarVenta(validacion: Validacion, estado: Estado) = {

    val e = estados += estado
    val valid = validaciones += validacion
    val fullQuery = DBIO.seq(valid, e)
    Db.db.run(fullQuery)
  }


}
