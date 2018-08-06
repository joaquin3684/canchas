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

  def ventasRecuperables(implicit obs: Seq[String]) : Future[Seq[(Venta, Estado)]] = {

    val obsSql = obs.mkString("'", "', '", "'")

    val a = for {
      e <- estados.filter(x =>
        (x.estado === RECHAZO_LOGISTICA ||
          x.estado === RECHAZO_AUDITORIA ||
          (x.estado === RECHAZO_VALIDACION && (x.observacion like "cantidad impagos%") || (x.observacion like "meses de traspaso%"))) &&
      x.recuperable === true && x.paraRecuperar === true)
      v <- ventas.filter( x => x.idObraSocial.inSetBind(obs) && x.id === e.idVenta)

    } yield (v, e)

/*
    val q = sql"""select ventas.dni, ventas.nombre, ventas.nacionalidad, ventas.domicilio, ventas.localidad, ventas.telefono, ventas.cuil, ventas.estadoCivil, ventas.edad, ventas.id_obra_social, ventas.fecha_nacimiento, ventas.zona, ventas.codigo_postal, ventas.hora_contacto_tel, ventas.piso, ventas.departamento, ventas.celular, ventas.hora_contacto_cel, ventas.base, ventas.empresa, ventas.cuit, ventas.tres_porciento, ventas.id, estados.user, estados.id_venta, estados.estado, estados.fecha, estados.recuperable, estados.observacion, estados.id, estados.para_recuperar from ventas
               join estados on ventas.id = estados.id_venta
            where (estados.estado = '#$RECHAZO_LOGISTICA'
             or estados.estado = '#$RECHAZO_AUDITORIA' or (estados.estado = '#$RECHAZO_VALIDACION' and (estados.observacion <> 'Hijo discapacitado' and estados.observacion <> 'Muchos hijos' and (estados.observacion like 'cantidad impagos%' or estados.observacion like 'meses de traspaso%')))) and estados.recuperable = true and estados.para_recuperar = true and ventas.id_obra_social in (#$obsSql)
     """.as[(Venta, Estado)]*/
    Db.db.run(a.result)

  }

  def ventasParaQueSeRecuperen(implicit obs: Seq[String]) : Future[Seq[(Venta, Estado)]] = {

    val obsSql = obs.mkString("'", "', '", "'")

    val a = for {
      e <- estados.filter(x =>
        (x.estado === RECHAZO_LOGISTICA ||
          x.estado === RECHAZO_AUDITORIA ||
          (x.estado === RECHAZO_VALIDACION && (x.observacion like "cantidad impagos%") || (x.observacion like "meses de traspaso%"))) &&
          x.recuperable === true && x.paraRecuperar === false)
      v <- ventas.filter( x => x.idObraSocial.inSetBind(obs) && x.id === e.idVenta)

    } yield (v, e)


/*
    val q = sql"""select ventas.*,  estados.* from ventas
               join estados on ventas.id = estados.id_venta
            where (estados.estado = '#$RECHAZO_LOGISTICA'
             or estados.estado = '#$RECHAZO_AUDITORIA' or (estados.estado = '#$RECHAZO_VALIDACION' and (estados.observacion <> 'Hijo discapacitado' and estados.observacion <> 'Muchos hijos' and estados.observacion like 'cantidad impagos%'))) and estados.recuperable = true and ventas.id_obra_social in (#$obsSql)
     """.as[(Venta, Estado)]
*/

    Db.db.run(a.result)
  }

  def recuperarVenta(idEstado: Long) = {
    Db.db.run(estados.filter(_.id === idEstado).delete)
  }

  def rechazarVenta(idEstado: Long) = {
    Db.db.run(estados.filter(_.id === idEstado).map(x => x.recuperable).update(false))
  }

  def marcarParaRecuperar(idEstado: Long) = {
    Db.db.run(estados.filter(_.id === idEstado).map(_.paraRecuperar).update(true))
  }

}
