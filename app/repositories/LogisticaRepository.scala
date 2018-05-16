package repositories

import java.sql.Timestamp

import akka.http.scaladsl.model.DateTime
import models._
import schemas.Schemas
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas.{estados, ventas, visitas}
import slick.jdbc.GetResult

import scala.concurrent.Future

class LogisticaRepository {
  implicit val localDateTimeMapping  = MappedColumnType.base[DateTime, Timestamp](
    dt => new Timestamp(dt.clicks),
    ts => DateTime(ts.getTime)
  )


 implicit val impVenta = GetResult(r => Venta(r.<<, r.<<, r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<, r.<<, r.<<))

  def ventasSinVisita()(implicit obs: Seq[String]): Future[Seq[Venta]] = {
    val query = {
      for {
        e <- estados.filter(x => x.estado === "Validado" && !(x.idVenta in estados.filter(x => x.estado === "Visita creada" || x.estado === "Rechazo por logistica").map(_.idVenta)))
        v <- ventas.filter(x => x.dni === e.idVenta && x.idObraSocial.inSetBind(obs))
      } yield v
    }
    Db.db.run(query.result)
  }

  def create(visita: Visita) = {
    val vi = visitas += visita
    val es = Estado(visita.idUser, visita.idVenta, "Visita creada", DateTime.now)
    val e = estados += es
    val fullquery = DBIO.seq(vi, e)
    Db.db.run(fullquery.transactionally)
  }

  def repactar(visita: Visita) = {
    val vi = visitas += visita
    val es = Estado(visita.idUser, visita.idVenta, "Visita repactada", DateTime.now)
    val e = estados += es
    val fullquery = DBIO.seq(vi, e)
    Db.db.run(fullquery.transactionally)
  }

  def rechazar(visita: Visita) = {
    val vi = visitas += visita
    val es = Estado(visita.idUser, visita.idVenta, "Rechazo por logistica", DateTime.now)
    val e = estados += es
    val fullquery = DBIO.seq(vi, e)
    Db.db.run(fullquery.transactionally)
  }

  def ventasAConfirmar()(implicit obs: Seq[String]): Future[Seq[(Venta, String)]] = {

    val obsSql = obs.mkString("'", "', '", "'")
    val p = sql"""select ventas.*,
              Case when (estados.estado = 'Visita creada') then 'Confirmar'
              else 'Pendiente' END AS is_a_senior
               from estados
        join ventas on ventas.dni = estados.id_venta
        join visitas on visitas.id_venta = ventas.dni
        where ((estados.estado = 'Visita creada' or estados.estado = 'Visita repactada') and
         not(estados.id_venta in (select id_venta from estados where estado = 'Visita confirmada' or estado = 'Rechazo por logistica')) and
         ventas.id_obra_social in (#$obsSql) and DATE(visitas.fecha) = ADDDATE(CURDATE(), INTERVAL 1 DAY)) or ((estados.estado = 'Auditoria aprobada' or estados.estado = 'Auditoria observada') and (estados.estado <> 'Rechazo por auditoria' and estados.estado <> 'Visita creada'))
      """.as[(Venta, String)]

    Db.db.run(p)
  }



  def getVisitas(dni: Int)(implicit obs: Seq[String]): Future[Seq[Visita]] = {
    val query = {
      for {
        v <- ventas.filter(x => x.dni === dni && x.idObraSocial.inSetBind(obs))
        vis <- visitas.filter(x => x.idVenta === v.dni)
      } yield  vis
    }
    Db.db.run(query.result)
  }

  def getVisita(dni: Int)(implicit obs: Seq[String]): Future[Visita] = {
    val query = {
      for {
        v <- ventas.filter(x => x.dni === dni && x.idObraSocial.inSetBind(obs))
        vis <- visitas.filter(x => x.idVenta === v.dni).sortBy(_.id.desc).take(1)

      } yield  vis
    }
    Db.db.run(query.result.head)
  }

  def all(user: String): Future[Seq[Venta]] = {
    val query = {
      for {
        e <- estados.filter(x => (x.estado === "Visita creada" || x.estado === "Visita repactada" || x.estado === "Visita confirmada" || x.estado === "Rechazo por logistica") && x.user === user ).map(_.idVenta)
        v <- ventas.filter(x => x.dni === e ).distinct
      } yield v
    }
    Db.db.run(query.result)
  }



}
