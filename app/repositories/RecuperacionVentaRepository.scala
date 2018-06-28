package repositories
import java.sql.Timestamp

import akka.http.scaladsl.model.DateTime

import scala.concurrent.Future
import models._
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas.{auditorias, estados, usuarios, usuariosPerfiles, validaciones, ventas}

object RecuperacionVentaRepository extends Estados{

  implicit val localDateTimeMapping  = MappedColumnType.base[DateTime, Timestamp](
    dt => new Timestamp(dt.clicks),
    ts => DateTime(ts.getTime)
  )

/*  def ventasRecuperables(implicit obs: Seq[String]) : Future[Seq[(Venta, Estado)]] = {

    val query = for{
      e <- estados.filter( x => (x.estado === RECHAZO_LOGISTICA || (x.estado === RECHAZO_VALIDACION  && (x.observacion =!= "Hijo discapacitado" && x.observacion =!=  "Muchos hijos" && x.observacion === "cantidad impagos%")) || x.estado === RECHAZO_AUDITORIA) && x.recuperable === true )
      v <- ventas.filter( x => x.dni === e.dni && x.idObraSocial.inSetBind(obs))
    } yield (v, e)


    val q = """ select ventas.*, estados.* from ventas
               join estados on ventas.dni = estados.id_venta
            where (estados.estado = '""" + RECHAZO_LOGISTICA +
      """' or estados.estado = '""" + RECHAZO_AUDITORIA +
      """' or (estados.estado <> '""" + RECHAZO_VALIDACION +
      """' and (estados.observacion <> 'Hijo discapacitado' and estados.observacion <> 'Muchos hijos' and estados.observacion like 'cantidad impagos%'))) and estados.recuperable = true

     """

    val p = sql"""q""".as[(Venta, String)]
    Db.db.run(query.result)
  }*/

  def recuperarVenta(idEstado: Long) = {
    Db.db.run(estados.filter(_.id === idEstado).delete)
  }



}
