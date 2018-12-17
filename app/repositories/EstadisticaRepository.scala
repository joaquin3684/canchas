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


  def rechazos(fechaDesde: DateTime, fechaHasta:DateTime)(implicit obs:Seq[String]): Future[Seq[(String, String, String, String, String, String, String, String)]] = {
    val obsSql = obs.mkString("'", "', '", "'")

    val fStr = fechaDesde.toIsoDateString()
    val fhStr = fechaHasta.toIsoDateString()


    val p = sql"""select
                    (select Date(fecha) from estados where id_venta = v.id and estado = 'Creado' limit 1) as fecha_creacion,
                     v.nombre,
                     v.cuil,
                     e.estado,
                     Date(e.fecha),
                     e.observacion,
                     e.recuperable,
                     (select u.nombre from estados e join usuarios u on u.user = e.user where e.estado = 'Creado' and e.id_venta = v.id) as vendedor
                  from ventas v
                  join estados e on e.id_venta = v.id
                  where e.recuperable = 0 and e.estado like 'Rech%' and e.fecha between '#$fStr' and '#$fhStr'

      """.as[(String, String, String, String, String, String, String, String)]

    Db.db.run(p)

  }

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

  def estadisticasVisitas(fechaDesde: DateTime, fechaHasta: DateTime, desdeVisita: DateTime, hastaVisita: DateTime)(implicit obs:Seq[String]): Future[Seq[(Visita, Venta, Estado)]] = {
    val q = for {
      vis <- visitas.filter(x => (x.fecha between(desdeVisita, hastaVisita)))
      v <- ventas.filter(x => x.idObraSocial.inSetBind(obs) && vis.idVenta === x.id )
      e <- estados.filter(x => x.idVenta === v.id)
      e2 <- estados.filter( x => (x.fecha between(fechaDesde, fechaHasta)) && x.estado === CREADO && x.idVenta === v.id)
    } yield (vis, v, e)

    Db.db.run(q.result)

  }

  def archivos(fechaDesde: DateTime, fechaHasta: DateTime)(implicit obs:Seq[String]): Future[Seq[(Venta, Auditoria, DateTime)]] = {
    val q = for {
      v <- ventas.filter(x => x.idObraSocial.inSetBind(obs))
      e <- estados.filter(x => x.idVenta === v.id && (x.estado === AUDITORIA_APROBADA || x.estado === AUDITORIA_OBSERVADA || x.estado === RECHAZO_AUDITORIA))
      a <- auditorias.filter(x => x.idVenta === v.id)
      e2 <- estados.filter(x => x.idVenta === v.id && x.estado === CREADO && (x.fecha between(fechaDesde, fechaHasta)))
    } yield (v, a, e2.fecha)

    Db.db.run(q.result)
  }

  def cantidadVentasPerfil(fechaDesde: DateTime, fechaHasta: DateTime, perfil: String)(implicit obs:Seq[String]): Future[Seq[(String, Int, Int, Int)]] = {

    val obsSql = obs.mkString("'", "', '", "'")

    val fStr = fechaDesde.toIsoDateString()
    val fhStr = fechaHasta.toIsoDateString()


    val p = sql"""select
                  usuarios.nombre,
                  (select count(*) from estados
                   where estados.user = e2.user and
                    estados.estado = 'Creado' and
                     estados.id_venta in
                     (select estados.id_venta from estados
                   where estados.estado like 'Rech%')) as rechazados,

                 (select count(*) from estados
                                    where estados.user = e2.user and
                                     estados.estado = 'Creado' and
                                      estados.id_venta in
                                      (select estados.id_venta from estados
                                    where estados.estado = 'Presentada')) as presentadas,

                 (select count(*) from estados
                                                     where estados.user = e2.user and
                                                      estados.estado = 'Creado' and
                                                       estados.id_venta in
                                                       (select estados.id_venta from estados
                                                     where estados.estado = 'Pagada')) as presentadas



                  from estados e2
                  join usuarios on e2.user = usuarios.user
                  join usuario_perfil on usuarios.user = usuario_perfil.user
                  where  e2.estado = 'Creado' and usuario_perfil.perfil = '#$perfil' and fecha between '#$fStr' and '#$fhStr'
                  group by usuarios.nombre, usuarios.user

      """.as[(String, Int, Int, Int)]


    Db.db.run(p)
  }

  def cantidadVisitasPerfil(fechaDesde: DateTime, fechaHasta: DateTime, perfil: String): Future[Seq[(String, Int)]] = {

    val fStr = fechaDesde.toIsoDateString()
    val fhStr = fechaHasta.toIsoDateString()


    val p = sql"""select
                  usuarios.nombre,
                  count(*) as cantVisitas
                  from visitas
                  join usuarios on visitas.id_user = usuarios.user
                  join usuario_perfil on usuarios.user = usuario_perfil.user
                  where visitas.estado = 'Visita confirmada' and usuario_perfil.perfil = '#$perfil' and visitas.fecha between '#$fStr' and '#$fhStr'
                  group by usuarios.nombre, usuarios.user

      """.as[(String, Int)]


    Db.db.run(p)
  }




}