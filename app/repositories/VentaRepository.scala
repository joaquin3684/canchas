package repositories

import java.sql.Timestamp

import akka.http.scaladsl.model.DateTime
import models._
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas.{estados, ventas}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

object VentaRepository extends Estados {



  implicit val localDateTimeMapping  = MappedColumnType.base[DateTime, Timestamp](
    dt => new Timestamp(dt.clicks),
    ts => DateTime(ts.getTime)
  )


  def checkObraSocial(idVenta: Long)(implicit obs: Seq[String]): Future[Option[Venta]] =  {

    Db.db.run(ventas.filter( v => v.idObraSocial.inSetBind(obs) && v.id === idVenta).result.headOption)

  }

  def checkDni(dni: Int): Future[Option[(Venta, Estado)]] = {
    val q = for {
      v <- ventas.filter(x => x.dni === dni).sortBy(_.id.asc).take(1)
      e <- estados.filter(x => x.idVenta === v.id).sortBy(_.id.asc).take(1)
    } yield (v, e)

    Db.db.run(q.result.headOption)
  }

  def create(venta: Venta, user: String, fecha: DateTime) = {
    val ventaConId =  (ventas returning ventas.map(_.id)
        into ((ven,id) => ven.copy(id = id))
        ) += venta
    val v = ventas += venta
    val estado = Estado(user, 2, CREADO, fecha)
    val e = estados += estado
    val fullQuery = DBIO.seq(v, e)

    Db.db.run(ventaConId)

  }

  def all(user: String)(implicit obs: Seq[String]) : Future[Seq[(Venta, Estado)]] = {
    val query = {
      for {
        e <- estados.filter( x => x.user === user && x.estado === CREADO)
        v <- ventas.filter(_.id === e.idVenta)
      } yield (v,e)
    }
    Db.db.run(query.result)
  }

  def agregarEstado(estado: Estado) = {
    val e = estados += estado
    Db.db.run(e)
  }

  def modificarVenta(venta: Venta, idVenta:Long, user: String, fechaCreacion: DateTime) = {

    val deleteEstado = estados.filter(e => e.idVenta === idVenta && e.estado === CREADO).delete
    val updateV = ventas.filter(_.id === idVenta).map( x => (x.dni, x.nombre, x.nacionalidad, x.domicilio, x.localidad, x.telefono, x.cuil, x.estadoCivil, x.edad, x.idObraSocial, x.fechaNacimiento, x.zona, x.codigoPostal, x.horaContactoTel, x.piso, x.dpto, x.celular, x.horaContactoCel, x.base, x.empresa, x.cuit, x.tresPorciento)).update((venta.dni, venta.nombre, venta.nacionalidad, venta.domicilio, venta.localidad, venta.telefono, venta.cuil, venta.estadoCivil, venta.edad, venta.idObraSocial, venta.fechaNacimiento, venta.zona, venta.codigoPostal, venta.horaContactoTel, venta.piso, venta.dpto, venta.celular, venta.horaContactoCel, venta.base, venta.empresa, venta.cuit, venta.tresPorciento))
    val estadoModificado = Estado(user, idVenta, CREADO, fechaCreacion)
    val updateEs = estados += estadoModificado
    val fullQuery = DBIO.seq(deleteEstado, updateV, updateEs)

    Db.db.run(fullQuery.transactionally)
  }
}
