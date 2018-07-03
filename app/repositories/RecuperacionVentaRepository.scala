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
  implicit val impVenta = GetResult( r => (Venta(r.<<, r.<<, r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<, DateTime(r.nextTimestamp().getTime),r.<<,r.<<,r.<<,r.<<,r.<<, r.<<, r.<<, r.<<), Estado(r.<<, r.<<, r.<<, DateTime(r.nextTimestamp().getTime), r.<<, r.<<, r.<<)))
  implicit val impEs = GetResult( r => Estado(r.<<, r.<<, r.<<, DateTime(r.nextTimestamp().getTime), r.<<, r.<<, r.<<))

  def ventasRecuperables(implicit obs: Seq[String]) : Future[Seq[(Venta, Estado)]] = {

    val obsSql = obs.mkString("'", "', '", "'")

    val q = sql"""select ventas.dni, ventas.nombre, ventas.nacionalidad, ventas.domicilio, ventas.localidad, ventas.telefono, ventas.cuil, ventas.estadoCivil, ventas.edad, ventas.id_obra_social, ventas.fecha_nacimiento, ventas.zona, ventas.codigo_postal, ventas.hora_contacto_tel, ventas.piso, ventas.departamento, ventas.celular, ventas.hora_contacto_cel, ventas.base, estados.*, usuario_perfil.perfil from ventas
               join estados on ventas.dni = estados.id_venta
               join usuarios on usuarios.user = estados.user
               join usuario_perfil on usuarios.user = usuario_perfil.user

            where (estados.estado = '#$RECHAZO_LOGISTICA'
             or estados.estado = '#$RECHAZO_AUDITORIA' or (estados.estado = '#$RECHAZO_VALIDACION' and (estados.observacion <> 'Hijo discapacitado' and estados.observacion <> 'Muchos hijos' and estados.observacion like 'cantidad impagos%'))) and estados.recuperable = true and ventas.id_obra_social in (#$obsSql)
     """.as[(Venta, Estado)]

    Db.db.run(q)
  }

  def recuperarVenta(idEstado: Long) = {
    Db.db.run(estados.filter(_.id === idEstado).delete)
  }

  def rechazarVenta(idEstado: Long) = {
    Db.db.run(estados.filter(_.id === idEstado).map(x => x.recuperable).update(false))
  }


}
