package repositories
import java.sql.Timestamp

import akka.http.scaladsl.model.DateTime

import scala.concurrent.Future
import models._
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas.{auditorias, estados, usuarios, usuariosPerfiles, validaciones, ventas}

object AdministracionVentaRepository extends Estados{

  implicit val localDateTimeMapping  = MappedColumnType.base[DateTime, Timestamp](
    dt => new Timestamp(dt.clicks),
    ts => DateTime(ts.getTime)
  )


  def ventasRechazables(implicit obs: Seq[String]) : Future[Seq[Venta]] = {

    val query = for {
      e <- estados.filter( x => x.estado === VISITA_CONFIRMADA && !(x.idVenta in estados.filter(x => x.estado === PRESENTADA).map(_.idVenta)))
      v <- ventas.filter(x => x.id === e.idVenta && x.idObraSocial.inSetBind(obs))
    }yield v

    val query2 = for{
      e <- estados.filter( x => x.estado === VALIDADO && !(x.idVenta in estados.filter(x => x.estado === PRESENTADA).map(_.idVenta)))
      v <- ventas.filter(x => x.id === e.idVenta && x.idObraSocial.inSetBind(obs))
      e2 <- estados.filter( x => x.estado === CREADO && x.idVenta === v.id)
      u <- usuarios.filter(_.user === e2.user)
      up <- usuariosPerfiles.filter(x => x.idUsuario === u.user && x.idPerfil =!= "OPERADOR VENTA")
    } yield v

    val unionQuery = query ++ query2

    Db.db.run(unionQuery.result)
  }

  def ventasIncompletas(implicit obs: Seq[String]) : Future[Seq[Venta]] = {

    val query = for{
      e <- estados.filter( x => x.estado === VISITA_CONFIRMADA)
      v <- ventas.filter(x => x.id === e.idVenta && x.idObraSocial.inSetBind(obs) && (x.empresa.isEmpty || x.cuit.isEmpty || x.tresPorciento.isEmpty))
    } yield v

    val query2 = for{
      e <- estados.filter( x => x.estado === VALIDADO)
      v <- ventas.filter(x => x.id === e.idVenta && x.idObraSocial.inSetBind(obs) && (x.empresa.isEmpty || x.cuit.isEmpty || x.tresPorciento.isEmpty))
      e2 <- estados.filter( x => x.estado === CREADO && x.idVenta === v.id)
      u <- usuarios.filter(_.user === e2.user)
      up <- usuariosPerfiles.filter(x => x.idUsuario === u.user && x.idPerfil =!= "OPERADOR VENTA")
    } yield v

    val unionQuery = query ++ query2

    Db.db.run(unionQuery.result)
  }

  def completarVenta(id: Long, empresa: String, cuit: String, tresPorciento: Double) = {
    val audiUp = ventas.filter(_.id === id).map( x => (x.empresa, x.cuit, x.tresPorciento)).update((Some(empresa), Some(cuit), Some(tresPorciento)))
    Db.db.run(audiUp)
  }

  def ventasPresentables(implicit obs: Seq[String]) : Future[Seq[(Venta, Auditoria, String, String, DateTime)]] = {

    val query = for{
      e <- estados.filter( x => x.estado === VISITA_CONFIRMADA && !(x.idVenta in estados.filter(x => x.estado === PRESENTADA).map(_.idVenta)))
      v <- ventas.filter(x => x.id === e.idVenta && x.idObraSocial.inSetBind(obs) && !x.empresa.isEmpty && !x.cuit.isEmpty && !x.tresPorciento.isEmpty)
      audi <- auditorias.filter(_.idVenta === v.id)
      e2 <- estados.filter(x => x.estado === CREADO && x.idVenta === v.id)
      u <- usuarios.filter(_.user === e2.user)
      up <- usuariosPerfiles.filter(_.idUsuario === u.user)
    } yield (v, audi, u.nombre, up.idPerfil, e2.fecha)

    val query2 = for {
      e <- estados.filter( x => x.estado === VALIDADO && !(x.idVenta in estados.filter(x => x.estado === PRESENTADA).map(_.idVenta)))
      v <- ventas.filter(x => x.id === e.idVenta && x.idObraSocial.inSetBind(obs) && !x.empresa.isEmpty && !x.cuit.isEmpty && !x.tresPorciento.isEmpty)
      audi <- auditorias.filter(_.idVenta === v.id)
      e2 <- estados.filter(x => x.estado === CREADO && x.idVenta === v.id)
      u <- usuarios.filter(_.user === e2.user)
      up <- usuariosPerfiles.filter(x => x.idUsuario === u.user && x.idPerfil =!= "OPERADOR VENTA")
    } yield (v, audi, u.nombre, up.idPerfil, e2.fecha)

    val unionQuery = query2 ++ query

    Db.db.run(unionQuery.result)
  }

  def presentarVentas(presentaciones: Seq[Long], fechaPresentacion: DateTime, user:String) = {
      val est = presentaciones.map( x => Estado(user, x, PRESENTADA, fechaPresentacion))
      val nuevos = estados ++= est
      Db.db.run(nuevos.transactionally)
  }

  def ventasPresentadas(implicit obs: Seq[String]) : Future[Seq[(Venta, DateTime)]] = {
    val query = for {
      e <- estados.filter( x => x.estado === PRESENTADA && !(x.idVenta in estados.filter(x => x.estado === PAGADA || x.estado === RECHAZO_ADMINISTRACION).map(_.idVenta)))
      v <- ventas.filter(x => x.id === e.idVenta && x.idObraSocial.inSetBind(obs))
    } yield (v, e.fecha)

    Db.db.run(query.result)
  }

  def analizarPresentacion(estado: Estado) = {
    val query = if(estado.estado == "estado auxiliar") estados.filter( x => x.idVenta === estado.idVenta && x.estado === PRESENTADA).delete else estados += estado
    Db.db.run(query)

  }

  def ventasPagadas(implicit obs: Seq[String]) : Future[Seq[(Venta, DateTime)]]= {
    val query = for{
      e <- estados.filter( x => x.estado === PAGADA && !(x.idVenta in estados.filter(x => x.estado === DIGITALIZADA ).map(_.idVenta)))
      v <- ventas.filter(x => x.id === e.idVenta && x.idObraSocial.inSetBind(obs))
      e2 <- estados.filter(x => x.estado === PRESENTADA && x.idVenta === v.id)
    } yield (v, e2.fecha)
    Db.db.run(query.result)
  }

}
