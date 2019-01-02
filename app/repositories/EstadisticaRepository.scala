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


  def zonas() : Future[Seq[String]] = {
    val q = sql"""select zona from ventas group by zona""".as[String]

    Db.db.run(q)
  }

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
                  where e.estado like 'Rech%' and exists (select 1 from estados where fecha between '#$fStr' and '#$fhStr' and estado = 'Creado' and id_venta = v.id)

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
                                  u.nombre,
                                   (select count(*) from estados
                                    where estados.user = u.user and
                                     estados.estado = 'Creado' and (fecha between '#$fStr' and '#$fhStr') and
                                      estados.id_venta in
                                      (select estados.id_venta from estados
                                    where estados.estado like 'Rech%')) as rechazados,

                                    (select count(*) from estados
                                                     where estados.user = u.user and
                                                      estados.estado = 'Creado' and (fecha between '#$fStr' and '#$fhStr') and
                                                       estados.id_venta in
                                                       (select estados.id_venta from estados
                                                     where estados.estado = 'Presentada')
                                                           and estados.id_venta not in
                                                    (select estados.id_venta from estados
                                                                     where estados.estado = 'Pagada' or estados.estado like 'Rech%')
                                  ) as presentadas,

                  (select count(*) from estados
                                                                      where estados.user = u.user and
                                                                       estados.estado = 'Creado' and (fecha between '#$fStr' and '#$fhStr') and
                                                                        estados.id_venta in
                                                                        (select estados.id_venta from estados
                                                                      where estados.estado = 'Pagada')) as pagadas


                   from usuarios u
                                   join usuario_perfil on u.user = usuario_perfil.user
                                   where  usuario_perfil.perfil = '#$perfil'
                                   group by u.nombre, u.user
                                   having rechazados > 0 or presentadas > 0 or pagadas > 0

      """.as[(String, Int, Int, Int)]


    Db.db.run(p)
  }


  def cantidadVentasTotalPorObraSocial(fechaDesde: DateTime, fechaHasta: DateTime)(implicit obs:Seq[String]): Future[Seq[(String, Int, Int, Int)]] = {

    val obsSql = obs.mkString("'", "', '", "'")

    val fStr = fechaDesde.toIsoDateString()
    val fhStr = fechaHasta.toIsoDateString()


    val p = sql"""select
                                  obs.nombre,
                                   (select count(*) from estados
                                   join ventas on estados.id_venta = ventas.id
                                    where ventas.id_obra_social = obs.nombre
                                    and estados.estado = 'Creado'
                                    and (fecha between '#$fStr' and '#$fhStr')
                                    and  ventas.id in
                                      (select estados.id_venta from estados
                                    where estados.estado like 'Rech%')) as rechazados,

                                    (select count(*) from estados
                                                      join ventas on estados.id_venta = ventas.id
                                                     where ventas.id_obra_social = obs.nombre and
                                                      estados.estado = 'Creado' and (fecha between '#$fStr' and '#$fhStr') and
                                                       ventas.id in
                                                       (select estados.id_venta from estados
                                                     where estados.estado = 'Presentada')
                                                           and estados.id_venta not in
                                                    (select estados.id_venta from estados
                                                                     where estados.estado = 'Pagada' or estados.estado like 'Rech%')
                                  ) as presentadas,

                  (select count(*) from estados
                              join ventas on estados.id_venta = ventas.id
                                where ventas.id_obra_social = obs.nombre and
                              estados.estado = 'Creado' and (fecha between '#$fStr' and '#$fhStr') and
                                  ventas.id in
                                              (select estados.id_venta from estados
                                              where estados.estado = 'Pagada')) as pagadas


                  from obras_sociales obs
                                   group by obs.nombre
                                   having rechazados > 0 or presentadas > 0 or pagadas > 0

      """.as[(String, Int, Int, Int)]


    Db.db.run(p)
  }

  def cantidadVentasCall(fechaDesde: DateTime, fechaHasta: DateTime, perfil: String)(implicit obs:Seq[String]): Future[Seq[(String, Int, Int, Int, Int)]] = {

    val obsSql = obs.mkString("'", "', '", "'")

    val fStr = fechaDesde.toIsoDateString()
    val fhStr = fechaHasta.toIsoDateString()


    val p = sql"""select
                                  u.nombre,
                                   (select count(*) from estados
                                    where estados.user = u.user and
                                     estados.estado = 'Creado' and (fecha between '#$fStr' and '#$fhStr') and
                                      estados.id_venta in
                                      (select estados.id_venta from estados
                                    where estados.estado like 'Rech%')) as rechazados,

                                    (select count(*) from estados
                                                     where estados.user = u.user and
                                                      estados.estado = 'Creado' and (fecha between '#$fStr' and '#$fhStr') and
                                                       estados.id_venta in
                                                       (select estados.id_venta from estados
                                                     where estados.estado = 'Presentada')
                                                           and estados.id_venta not in
                                                    (select estados.id_venta from estados
                                                                     where estados.estado = 'Pagada' or estados.estado like 'Rech%')
                                  ) as presentadas,

                  (select count(*) from estados
                                                                      where estados.user = u.user and
                                                                       estados.estado = 'Creado' and (fecha between '#$fStr' and '#$fhStr') and
                                                                        estados.id_venta in
                                                                        (select estados.id_venta from estados
                                                                      where estados.estado = 'Pagada')) as pagadas,

                  (select count(*) from estados
                             where estados.user = u.user
                              and  estados.estado = 'Creado'
                               and (fecha between '#$fStr' and '#$fhStr')
                                and estados.id_venta in
                             (select estados.id_venta from estados
                                where estados.estado = 'Auditoria aprobada' or estados.estado = 'Auditoria observada')
                                 and estados.id_venta not in
                                           (select estados.id_venta from estados
                                              where estados.estado = 'Pagada' or estados.estado = 'Presentada' or estados.estado like 'Rech%'))as auditadas
                   from usuarios u
                                   join usuario_perfil on u.user = usuario_perfil.user
                                   where  usuario_perfil.perfil = '#$perfil'
                                   group by u.nombre, u.user
                                   having rechazados > 0 or presentadas > 0 or pagadas > 0 or auditadas > 0

      """.as[(String, Int, Int, Int, Int)]


    Db.db.run(p)
  }

  def empresas(fechaDesde: DateTime, fechaHasta:DateTime): Future[Seq[(Long, String, String, String, String, String, String, String)]] = {
    val fStr = fechaDesde.toIsoDateString()
    val fhStr = fechaHasta.toIsoDateString()

    val p = sql"""select datos_empresa.*, usuarios.nombre as vendedor from ventas
                    join estados on ventas.id = estados.id_venta
                    join usuarios on estados.user = usuarios.user
                    join datos_empresa on ventas.id = datos_empresa.id_venta
                    where estados.estado = 'Creado'

      """.as[(Long, String, String, String, String, String, String, String)]
    Db.db.run(p)

  }

  def localidadesEmpresa(fechaDesde: DateTime, fechaHasta:DateTime): Future[Seq[String]] = {
    val fStr = fechaDesde.toIsoDateString()
    val fhStr = fechaHasta.toIsoDateString()

    val p = sql"""select datos_empresa.localidad from ventas
                    join datos_empresa on ventas.id = datos_empresa.id_venta
                    group by datos_empresa.localidad

      """.as[String]
    Db.db.run(p)

  }


  def cantidadVentasTotalPorDia(fechaDesde: DateTime, fechaHasta: DateTime)(implicit obs:Seq[String]): Future[Seq[(String, Int, Int, Int)]] = {

    val obsSql = obs.mkString("'", "', '", "'")

    val fStr = fechaDesde.toIsoDateString()
    val fhStr = fechaHasta.toIsoDateString()


    val p = sql"""select e.fecha,
                               sum(case when exists (select 1 from estados where estados.id_venta = e.id_venta and estado like 'Rech%') then 1 else 0 end) as rechazados,
                               sum(case when not exists (select 1 from estados where estados.id_venta = e.id_venta and estado = 'Pagada' or estado like 'Rech%' ) then 1 else 0 end) as presentados,
                               sum(case when exists (select 1 from estados where estados.id_venta = e.id_venta and estado = 'Pagada' and not exists (select 1 from estados ea where estados.id_venta = ea.id_venta and estado like 'Rech%' )) then 1 else 0 end) as pagados

                           from estados e
                                           where (e.fecha between '#$fStr' and '#$fhStr') and e.estado = 'Presentada'
                                           group by e.fecha

      """.as[(String, Int, Int, Int)]


    Db.db.run(p)
  }

  def cantidadVentasTotalPorSemana(fechaDesde: DateTime, fechaHasta: DateTime)(implicit obs:Seq[String]): Future[Seq[(String, Int, Int, Int)]] = {

    val obsSql = obs.mkString("'", "', '", "'")

    val fStr = fechaDesde.toIsoDateString()
    val fhStr = fechaHasta.toIsoDateString()


    val p = sql"""select concat(month(e.fecha), '-', round((dayofyear(date(e.fecha)) - dayofyear(date('#$fStr')))/7)) as semana,
                               sum(case when exists (select 1 from estados where estados.id_venta = e.id_venta and estado like 'Rech%') then 1 else 0 end) as rechazados,
                               sum(case when not exists (select 1 from estados where estados.id_venta = e.id_venta and estado = 'Pagada' or estado like 'Rech%' ) then 1 else 0 end) as presentados,
                               sum(case when exists (select 1 from estados where estados.id_venta = e.id_venta and estado = 'Pagada' and not exists (select 1 from estados ea where estados.id_venta = ea.id_venta and estado like 'Rech%' )) then 1 else 0 end) as pagados

                           from estados e
                                           where (e.fecha between '#$fStr' and '#$fhStr') and e.estado = 'Presentada'
                                           group by semana
                                           order by cast(REPLACE(semana, '-', '') as unsigned) asc

      """.as[(String, Int, Int, Int)]


    Db.db.run(p)
  }

  def cantidadVentasTotalPorMes(fechaDesde: DateTime, fechaHasta: DateTime)(implicit obs:Seq[String]): Future[Seq[(String, Int, Int, Int)]] = {

    val obsSql = obs.mkString("'", "', '", "'")

    val fStr = fechaDesde.toIsoDateString()
    val fhStr = fechaHasta.toIsoDateString()


    val p = sql"""select concat(year(e.fecha), '-', month(e.fecha)),
                   sum(case when exists (select 1 from estados where estados.id_venta = e.id_venta and estado like 'Rech%') then 1 else 0 end) as rechazados,
                   sum(case when not exists (select 1 from estados where estados.id_venta = e.id_venta and estado = 'Pagada' or estado like 'Rech%' ) then 1 else 0 end) as presentados,
                   sum(case when exists (select 1 from estados where estados.id_venta = e.id_venta and estado = 'Pagada' and not exists (select 1 from estados ea where estados.id_venta = ea.id_venta and estado like 'Rech%' )) then 1 else 0 end) as pagados

                   from estados e
                        where (e.fecha between '#$fStr' and '#$fhStr') and e.estado = 'Presentada'
                        group by concat(year(e.fecha), '-', month(e.fecha))

      """.as[(String, Int, Int, Int)]


    Db.db.run(p)
  }

  def cantidadVentasTotalPorAnio(fechaDesde: DateTime, fechaHasta: DateTime)(implicit obs:Seq[String]): Future[Seq[(String, Int, Int, Int)]] = {

    val obsSql = obs.mkString("'", "', '", "'")

    val fStr = fechaDesde.toIsoDateString()
    val fhStr = fechaHasta.toIsoDateString()


    val p = sql"""select year(e.fecha),
                   sum(case when exists (select 1 from estados where estados.id_venta = e.id_venta and estado like 'Rech%') then 1 else 0 end) as rechazados,
                   sum(case when not exists (select 1 from estados where estados.id_venta = e.id_venta and estado = 'Pagada' or estado like 'Rech%' ) then 1 else 0 end) as presentados,
                   sum(case when exists (select 1 from estados where estados.id_venta = e.id_venta and estado = 'Pagada' and not exists (select 1 from estados ea where estados.id_venta = ea.id_venta and estado like 'Rech%' )) then 1 else 0 end) as pagados

                   from estados e
                        where (e.fecha between '#$fStr' and '#$fhStr') and e.estado = 'Presentada'
                        group by year(e.fecha)

      """.as[(String, Int, Int, Int)]


    Db.db.run(p)
  }

  def cantidadVentasPorZona(fechaDesde: DateTime, fechaHasta: DateTime)(implicit obs:Seq[String]): Future[Seq[(String, Int, Int, Int)]] = {

    val obsSql = obs.mkString("'", "', '", "'")

    val fStr = fechaDesde.toIsoDateString()
    val fhStr = fechaHasta.toIsoDateString()


    val p = sql"""select v.zona,
                       sum(case when exists (select 1 from estados where estados.id_venta = e.id_venta and estado like 'Rech%') then 1 else 0 end) as rechazados,
                       sum(case when not exists (select 1 from estados where estados.id_venta = e.id_venta and estado = 'Pagada' or estado like 'Rech%' ) then 1 else 0 end) as presentados,
                       sum(case when exists (select 1 from estados where estados.id_venta = e.id_venta and estado = 'Pagada' and not exists (select 1 from estados ea where estados.id_venta = ea.id_venta and estado like 'Rech%' )) then 1 else 0 end) as pagados



                          from ventas v
                          join estados e on e.id_venta = v.id
                          WHERE
                          (fecha between '#$fStr' and '#$fhStr') and
                          e.estado = 'Creado'
                          group by v.zona


      """.as[(String, Int, Int, Int)]


    Db.db.run(p)
  }

  def cantidadVentasPorLocalidad(fechaDesde: DateTime, fechaHasta: DateTime, zonas: Seq[String])(implicit obs:Seq[String]): Future[Seq[(String, Int, Int, Int)]] = {

    val zon = zonas.mkString("'", "', '", "'")

    val fStr = fechaDesde.toIsoDateString()
    val fhStr = fechaHasta.toIsoDateString()


    val p = sql"""select ventas.localidad,

                 sum(case when exists (select 1 from estados where estados.id_venta = e.id_venta and estado like 'Rech%') then 1 else 0 end) as rechazados,
                 sum(case when not exists (select 1 from estados where estados.id_venta = e.id_venta and estado = 'Pagada' or estado like 'Rech%' ) then 1 else 0 end) as presentados,
                 sum(case when exists (select 1 from estados where estados.id_venta = e.id_venta and estado = 'Pagada' and not exists (select 1 from estados ea where estados.id_venta = ea.id_venta and estado like 'Rech%' )) then 1 else 0 end) as pagados

                          from ventas
                          join estados e on e.id_venta = ventas.id
                          where ventas.zona in (#$zon) and
                          (fecha between '#$fStr' and '#$fhStr') and
                          e.estado = 'Creado'
                          group by ventas.localidad


      """.as[(String, Int, Int, Int)]


    Db.db.run(p)
  }

  def indicadorVentasPresentadasDelMes()(implicit obs:Seq[String]): Future[Seq[(Int, Int, Int, Int, Int, Int)]] = {

    val obsSql = obs.mkString("'", "', '", "'")



    val p = sql"""select (select count(distinct id_venta) from estados
                            where estado = '#$PRESENTADA'
                            and month(fecha) = month(CURDATE())
                            and id_venta not in (select id_venta from estados
                                                          where estado = 'Pagada' or estado like 'Rech%')) as presentadas,

                          (select count(distinct id_venta) from estados
                                    where estado = '#$PAGADA'
                                    and id_venta in (select id_venta from estados where estado = '#$PRESENTADA' and month(fecha) = month(CURDATE()))
                                    and id_venta not in (select id_venta from estados
                                           where estado like 'Rech%')) as pagadas,

                         (select count(distinct id_venta) from estados
                                    where estado like 'Rech%'
                                    and id_venta in (select id_venta from estados where estado = '#$PRESENTADA' and month(fecha) = month(CURDATE()))
                                    ) as rechazadas,

                         (select count(distinct id_venta) from estados
                                where estado = '#$PRESENTADA'
                                  and month(fecha) = month(CURDATE()- INTERVAL 1 MONTH)
                                  and id_venta not in (select id_venta from estados
                                      where estado = 'Pagada' or estado like 'Rech%')) as presentadasMesAnterior,

                          (select count(distinct id_venta) from estados
                                    where estado = '#$PAGADA'
                                    and id_venta in (select id_venta from estados where estado = '#$PRESENTADA' and month(fecha) = month(CURDATE() - INTERVAL 1 MONTH))
                                    and id_venta not in (select id_venta from estados
                                           where estado like 'Rech%')) as pagadasMesAnterior,
                          (select count(distinct id_venta) from estados
                                    where estado like 'Rech%'
                                    and id_venta in (select id_venta from estados where estado = '#$PRESENTADA' and month(fecha) = month(CURDATE() - INTERVAL 1 MONTH))
                                    ) as rechazadasMesAnterior


                          from estados e
                          where e.estado = '#$PRESENTADA'
                           or e.estado = '#$PAGADA'
                           or e.estado like 'Rech%'

                          group by e.estado
                          limit 1

      """.as[(Int, Int, Int, Int, Int, Int)]


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