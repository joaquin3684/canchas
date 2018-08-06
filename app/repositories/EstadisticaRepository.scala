package repositories
import java.sql.Timestamp

import akka.http.scaladsl.model.DateTime

import scala.concurrent.Future
import models._
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas.{auditorias, estados, visitas, usuarios, usuariosPerfiles, validaciones, ventas}
import slick.jdbc.GetResult

object EstadisticaRepository extends Estados {

  implicit val localDateTimeMapping  = MappedColumnType.base[DateTime, Timestamp](
    dt => new Timestamp(dt.clicks),
    ts => DateTime(ts.getTime)
  )
  implicit val impVenta = GetResult( r => (Venta(r.<<, r.<<, r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<, Some(DateTime(r.nextTimestamp().getTime)),r.<<,r.<<,r.<<,r.<<,r.<<, r.<<, r.<<, r.<<), Estado(r.<<, r.<<, r.<<, DateTime(r.nextTimestamp().getTime), r.<<, r.<<, r.<<)))
  implicit val impEs = GetResult( r => Estado(r.<<, r.<<, r.<<, DateTime(r.nextTimestamp().getTime), r.<<, r.<<, r.<<))


  def estadisticaGeneral(fechaDesde: DateTime, fechaHasta: DateTime)(implicit obs: Seq[String]) : Future[Seq[(Venta, Estado, Option[Visita], Option[Validacion], Option[Auditoria], Usuario, String)]] = {

    val obsSql = obs.mkString("'", "', '", "'")
    val fd = fechaDesde.toIsoDateString()
    val fh = fechaHasta.toIsoDateString()

    val q = for {
      (((((((v, e), e2), u), p), vis), vali),audi) <-
      ventas.filter(x => x.idObraSocial.inSetBind(obs))
        .join(estados).on(_.id === _.idVenta)
          .join(estados.filter(x => (x.fecha between(fechaDesde, fechaHasta)) && x.estado === CREADO)).on(_._1.id === _.idVenta)
            .join(usuarios).on(_._2.user === _.user)
              .join(usuariosPerfiles).on(_._2.user === _.idUsuario)
                .joinLeft(visitas).on(_._1._1._1._1.id === _.idVenta)
                  .joinLeft(validaciones).on(_._1._1._1._1._1.id === _.idVenta)
                    .joinLeft(auditorias).on(_._1._1._1._1._1._1.id === _.idVenta)


    } yield(v, e, vis, vali, audi, u, p.idPerfil)

    Db.db.run(q.result)

  }

  def states() : Future[Seq[String]] = {
    val q = sql"""select estados.estado from estados group by estados.estado""".as[String]

    Db.db.run(q)
  }
}