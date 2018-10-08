package repositories

import java.sql.Timestamp

import akka.http.scaladsl.model.DateTime
import models._
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas.{estados, validaciones, ventas, datosEmpresas}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

object ValidacionRepository extends Estados {

  implicit val localDateTimeMapping  = MappedColumnType.base[DateTime, Timestamp](
    dt => new Timestamp(dt.clicks),
    ts => DateTime(ts.getTime)
  )

  def checkObraSocial(id: Long)(implicit obs: Seq[String]): Option[Venta] =  {

    val v = Db.db.run(ventas.filter( v => v.idObraSocial.inSetBind(obs) && v.id === id).result.headOption)
    Await.result(v, Duration.Inf)
  }

  def all(user: String): Future[Seq[Venta]] = {
    val query = {
      for {
        e <- estados.filter( x => x.user === user && (x.estado === VALIDADO || x.estado === RECHAZO_VALIDACION))
        v <- ventas.filter(_.id === e.idVenta)
      } yield v
    }
    Db.db.run(query.result)

  }

  def ventasModificables (implicit obs: Seq[String]): Future[Seq[(Venta, Estado)]]= {
    val query = {
      for {
        e <- estados.filter(x => x.estado =!= DIGITALIZADA && x.estado =!= RECHAZO_AUDITORIA && x.estado =!= RECHAZO_ADMINISTRACION && x.estado =!= RECHAZO_VALIDACION && x.estado =!= RECHAZO_LOGISTICA && x.estado =!= RECHAZO_PRESENTACION && x.estado === CREADO)
        v <- ventas.filter(x => x.id === e.idVenta && x.idObraSocial.inSetBind(obs))
      } yield (v, e)
    }
    Db.db.run(query.result)

  }


  def ventasAValidar(user: String)(implicit obs: Seq[String]) : Future[Seq[(Venta, DateTime, String)]] = {
    val query = {
      for {
        e <- estados.filter(x => x.estado === CREADO && !(x.idVenta in estados.filter(x => x.estado === VALIDADO || x.estado === RECHAZO_VALIDACION).map(_.idVenta)))
        v <- ventas.filter(x => x.id === e.idVenta && x.idObraSocial.inSetBind(obs))
      } yield (v, e.fecha, e.user)
    }
    Db.db.run(query.result)

  }

  def validarVenta(validacion: Validacion, estado: Estado, datos: DatosEmpresa) = {

    val e = estados += estado
    val valid = validaciones += validacion
    val d = datosEmpresas += datos
    val fullQuery = DBIO.seq(valid, e, d)
    Db.db.run(fullQuery.transactionally)
  }


}
