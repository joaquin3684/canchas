package repositories

import java.sql.Timestamp

import akka.http.scaladsl.model.DateTime
import models._
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas.{estados, validaciones, ventas, datosEmpresas, usuariosPerfiles}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

object ValidacionRepository extends Estados with Perfiles {

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


  def ventasAValidar(user: String)(implicit obs: Seq[String]) : Future[Seq[(Venta, DateTime, String, String)]] = {
    val query = {
      for {
        e <- estados.filter(x => x.estado === CREADO && !(x.idVenta in estados.filter(x => x.estado === VALIDADO || x.estado === RECHAZO_VALIDACION).map(_.idVenta)))
        v <- ventas.filter(x => x.id === e.idVenta && x.idObraSocial.inSetBind(obs))
        up <- usuariosPerfiles.filter(x => e.user === x.idUsuario && (x.idPerfil === OPERADOR_VENTA || x.idPerfil === PROMOTORA || x.idPerfil === EXTERNO || x.idPerfil === VENDEDORA))
      } yield (v, e.fecha, e.user, up.idPerfil)
    }
    Db.db.run(query.result)

  }

  def validarVenta(validacion: Validacion, estado: Estado, datos: DatosEmpresa, capitas: Int) = {

    val e = estados += estado
    val delV = validaciones.filter(_.idVenta === validacion.idVenta).delete
    val valid = validaciones += validacion
    val delD = datosEmpresas.filter(_.idVenta === validacion.idVenta).delete
    val d = datosEmpresas += datos

    if(capitas == 99)
      {
        val fullQuery = DBIO.seq(delV, delD, valid, e, d)
        Db.db.run(fullQuery.transactionally)
      }
    else {
      val v = ventas.filter(x => x.id === validacion.idVenta).map(_.capitas).update(Some(capitas))


      val fullQuery = DBIO.seq(delV, delD, valid, e, d,v)
      Db.db.run(fullQuery.transactionally)
    }

  }


}
