package repositories
import java.sql.Timestamp

import akka.http.scaladsl.model.DateTime

import scala.concurrent.Future
import models._
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas.{auditorias, estados, usuarios, usuariosPerfiles, validaciones, ventas}
import slick.jdbc.GetResult

object RecuperacionVentaRepository extends Estados{

  implicit val localDateTimeMapping  = MappedColumnType.base[DateTime, Timestamp](
    dt => new Timestamp(dt.clicks),
    ts => DateTime(ts.getTime)
  )
  implicit val impVenta = GetResult( r => (Venta(r.<<, r.<<, r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<, Some(DateTime(r.nextTimestamp().getTime)),r.<<,r.<<,r.<<,r.<<,r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<), Estado(r.<<, r.<<, r.<<, DateTime(r.nextTimestamp().getTime), r.<<, r.<<, r.<<, r.<<)))
  //implicit val impEs = GetResult( r => Estado(r.<<, r.<<, r.<<, DateTime(r.nextTimestamp().getTime), r.<<, r.<<, r.<<, r.<<))

  def ventasRecuperables(user: String)(implicit obs: Seq[String]) : Future[Seq[(Venta, Estado)]] = {

    val obsSql = obs.mkString("'", "', '", "'")

    val a = for {
      e <- estados.filter(x =>
        (x.estado === RECHAZO_LOGISTICA ||
          x.estado === RECHAZO_AUDITORIA ||
          x.estado === RECHAZO_VALIDACION ||
          x.estado === RECHAZO_ADMINISTRACION) &&
      x.recuperable === true && x.paraRecuperar === true)
      v <- ventas.filter( x => x.idObraSocial.inSetBind(obs) && x.id === e.idVenta)
      e2 <- estados.filter(x => x.idVenta === v.id && x.user === user)
    } yield (v, e)

    Db.db.run(a.result)

  }

  def ventasParaQueSeRecuperen(implicit obs: Seq[String]) : Future[Seq[(Venta, Estado)]] = {

    val obsSql = obs.mkString("'", "', '", "'")

    val a = for {
      e <- estados.filter(x =>
        (x.estado === RECHAZO_LOGISTICA ||
          x.estado === RECHAZO_AUDITORIA ||
          x.estado === RECHAZO_VALIDACION ||
          x.estado === RECHAZO_ADMINISTRACION) &&
          x.recuperable === true && x.paraRecuperar === false)
      v <- ventas.filter( x => x.idObraSocial.inSetBind(obs) && x.id === e.idVenta)

    } yield (v, e)

    Db.db.run(a.result)
  }

  def recuperarVenta(idEstado: Long) = {
    Db.db.run(estados.filter(_.id === idEstado).delete)
  }

  def rechazarVenta(idEstado: Long, observacion: String) = {
    Db.db.run(estados.filter(_.id === idEstado).map(x => (x.observacion, x.recuperable)).update((Some(observacion), false)))
  }

  def marcarParaRecuperar(idEstado: Long, user:String, idVenta: Long) = {

    val a = estados.filter(_.id === idEstado).map(_.paraRecuperar).update(true)
    val b = estados.filter(x => x.idVenta === idVenta && x.estado === CREADO).map(_.user).update(user)
    val fullQuery = DBIO.seq(a, b)


    Db.db.run(fullQuery.transactionally)
  }

}
