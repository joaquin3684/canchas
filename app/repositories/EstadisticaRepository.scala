package repositories
import java.sql.Timestamp

import akka.http.scaladsl.model.DateTime

import scala.concurrent.Future
import models._
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas.{auditorias, estados,visitas, usuarios, usuariosPerfiles, validaciones, ventas}
import slick.jdbc.GetResult

object EstadisticaRepository extends Estados {

  implicit val localDateTimeMapping  = MappedColumnType.base[DateTime, Timestamp](
    dt => new Timestamp(dt.clicks),
    ts => DateTime(ts.getTime)
  )
  implicit val impVenta = GetResult( r => (Venta(r.<<, r.<<, r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<, DateTime(r.nextTimestamp().getTime),r.<<,r.<<,r.<<,r.<<,r.<<, r.<<, r.<<, r.<<), Estado(r.<<, r.<<, r.<<, DateTime(r.nextTimestamp().getTime), r.<<, r.<<, r.<<)))
  implicit val impEs = GetResult( r => Estado(r.<<, r.<<, r.<<, DateTime(r.nextTimestamp().getTime), r.<<, r.<<, r.<<))


  def estadisticaGeneral(fechaDesde: DateTime, fechaHasta: DateTime)(implicit obs: Seq[String]) : Future[Seq[(Venta, Estado, Option[Visita], Option[Validacion], Option[Auditoria])]] = {

    val obsSql = obs.mkString("'", "', '", "'")
    val fd = fechaDesde.toIsoDateString()
    val fh = fechaHasta.toIsoDateString()

    val q = for {
      (((((v, e), e2), vis), vali),audi) <-
      ventas.filter(x => x.idObraSocial.inSetBind(obs))
        .join(estados).on(_.dni === _.dni)
          .join(estados.filter(x => (x.fecha between(fechaDesde, fechaHasta)) && x.estado === CREADO)).on(_._1.dni === _.dni)
            .joinLeft(visitas).on(_._1._1.dni === _.dni)
              .joinLeft(validaciones).on(_._1._1._1.dni === _.dni)
              .joinLeft(auditorias).on(_._1._1._1._1.dni === _.dni)

    } yield(v, e, vis, vali, audi)

    Db.db.run(q.result)

    /*val p = sql"""select ventas.dni, ventas.nombre, ventas.cuil, ventas.telefono, ventas.nacionalidad, ventas.domicilio, ventas.localidad, ventas.estadoCivil, ventas.edad, ventas.id_obra_social, ventas.fecha_nacimiento, ventas.zona, ventas.codigo_postal, ventas.hora_contacto_tel, ventas.piso, ventas.departamento, ventas.celular, ventas.hora_contacto_cel, ventas.base, estados.*, visitas.*, validacion.*, auditorias.*
                                horaContactoTel,
                                from ventas
                         join estados on ventas.dni = estados.id_venta
                         left join visitas on ventas.dni = visitas.id_venta
                         left join validaciones on ventas.dni = validaciones.id_venta
                         left join auditorias on ventas.dni = auditorias.id_venta
                         join estados e2 on ventas.dni = e2.id_venta
                         where e2.estado ='#$CREADO' and e2.fecha BETWEEN '#$fd' and '#$fh'
      """.as[(Venta, String)]*/

  }
}