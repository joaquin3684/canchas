package repositories

import java.sql.Timestamp

import akka.http.scaladsl.model.DateTime
import models._
import schemas.Schemas
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas.{estados, ventas, visitas, usuarios, usuariosPerfiles, auditorias}
import slick.jdbc.GetResult

import scala.concurrent.Future

object LogisticaRepository extends Estados{

  implicit val localDateTimeMapping  = MappedColumnType.base[DateTime, Timestamp](
    dt => new Timestamp(dt.clicks),
    ts => DateTime(ts.getTime)
  )

  implicit val impVenta = GetResult( r => Venta(r.<<, r.<<, r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<, Some(DateTime.now), r.nextString(),r.<<,r.<<,r.<<,r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))

  def asignarUsuario(usuario: String, idVisita: Long) = {
    Db.db.run(visitas.filter(_.id === idVisita).map(_.user).update(Some(usuario)))
  }

  def ventasSinVisita()(implicit obs: Seq[String]): Future[Seq[(Venta, String)]] = {
    val query = {
      for {
        e <- estados.filter(x => (x.estado === AUDITORIA_APROBADA || x.estado === AUDITORIA_OBSERVADA) && !(x.idVenta in estados.filter(x => x.estado === VISITA_CREADA || x.estado === RECHAZO_LOGISTICA).map(_.idVenta)))
        v <- ventas.filter(x => x.id === e.idVenta && x.idObraSocial.inSetBind(obs))
        e2 <- estados.filter(x => x.estado === CREADO && e.idVenta === x.idVenta)
        u <- usuariosPerfiles.filter(x => x.idUsuario === e2.user && x.idPerfil === "OPERADOR VENTA")
        au <- auditorias.filter(x => x.idVenta === v.id)
      } yield (v, au.adherentes)
    }
    Db.db.run(query.result)
  }

  def create(visita: Visita, es: Estado) = {
    val vi = visitas += visita
    val e = estados += es
    val fullquery = DBIO.seq(vi, e)
    Db.db.run(fullquery.transactionally)
  }

  def repactar(visita: Visita, es: Estado) = {
    val vi = visitas += visita
    val e = estados += es
    val fullquery = DBIO.seq(vi, e)
    Db.db.run(fullquery.transactionally)
  }

  def confirmarVisita(idVisita: Long, estado: Estado) = {
    val vi = visitas.filter(_.id === idVisita).map(_.estado).update(VISITA_CONFIRMADA)
    val es = estados += estado
    val fullquery = DBIO.seq(vi, es)
    Db.db.run(fullquery.transactionally)
  }


  def rechazar(estado: Estado) = {
    val a = estados += estado
    val b = estados.filter(x => x.idVenta === estado.idVenta && (x.estado === VISITA_CREADA || x.estado === VISITA_REPACTADA)).delete
    val fullquery = DBIO.seq(a, b)
    Db.db.run(fullquery.transactionally)
  }

  def enviarACall(idVisita: Long, idVenta: Long) = {
    Db.db.run(estados.filter(x => x.idVenta === idVenta && (x.estado === VISITA_CREADA || x.estado === VISITA_REPACTADA)).delete)
  }

  def ventasAConfirmar()(implicit obs: Seq[String]): Future[Seq[(Venta, String, String, String, String)]] = {

    val obsSql = obs.mkString("'", "', '", "'")

    val p = sql"""select ventas.dni, ventas.nombre, ventas.nacionalidad, ventas.domicilio, ventas.localidad, ventas.telefono, ventas.cuil, ventas.estadoCivil, ventas.edad, ventas.id_obra_social, ventas.zona, ventas.codigo_postal, ventas.hora_contacto_tel, ventas.piso, ventas.departamento, ventas.celular, ventas.hora_contacto_cel, ventas.base, ventas.empresa, ventas.cuit, ventas.tres_porciento, ventas.id, DATE_FORMAT(DATE(visitas.fecha), '%d/%m/%Y'), visitas.hora as horaVisita, auditorias.adherentes,
              Case when (visitas.id_user IS NULL ) then 'Pendiente'
              else 'Confirmar' END AS is_a_senior
               from ventas
        join estados on ventas.id = estados.id_venta
        join visitas on visitas.id_venta = ventas.id
        left join auditorias on auditorias.id_venta = ventas.id
        where (estados.id_venta in (select id_venta from estados where estado = 'Visita creada' or estado = 'Visita repactada' group by id_venta) and
         estados.id_venta not in (select id_venta from estados where estado = 'Visita confirmada' or estado = 'Rechazo por logistica' group by id_venta) and
         ventas.id_obra_social in (#$obsSql) and visitas.id = (select id from visitas where id_venta = ventas.id order by id desc limit 1))
         or ((estados.id_venta in (select id_venta from estados where estado = 'Visita creada' or estado = 'Visita repactada' group by id_venta) and
                          estados.id_venta not in (select id_venta from estados where estado = 'Visita confirmada' or estado = 'Rechazo por logistica' group by id_venta) and
                          ventas.id_obra_social in (#$obsSql) and visitas.id_user IS NOT NULL and visitas.id = (select id from visitas where id_venta = ventas.id order by id desc limit 1)  ))
           group by ventas.dni, ventas.nombre, ventas.cuil, ventas.telefono, ventas.nacionalidad, ventas.domicilio, ventas.localidad, ventas.estadoCivil, ventas.edad, ventas.id_obra_social, ventas.fecha_nacimiento, ventas.zona, ventas.codigo_postal, ventas.hora_contacto_tel, ventas.piso, ventas.departamento, ventas.celular, ventas.hora_contacto_cel, ventas.base, ventas.empresa, ventas.cuit, ventas.tres_porciento, ventas.id, visitas.fecha, visitas.hora, auditorias.adherentes, Case when (visitas.id_user IS NULL ) then 'Pendiente'
                              else 'Confirmar' END
      """.as[(Venta, String, String, String, String)]
//    val j = group by ventas.dni, ventas.nombre, ventas.cuil, ventas.telefono, ventas.nacionalidad, ventas.domicilio, ventas.localidad, ventas.estadoCivil, ventas.edad, ventas.id_obra_social, ventas.fecha_nacimiento, ventas.zona, ventas.codigo_postal, ventas.hora_contacto_tel, ventas.piso, ventas.departamento, ventas.celular, ventas.hora_contacto_cel, ventas.base, ventas.empresa, ventas.cuit, ventas.tres_porciento, ventas.id, Case when (visitas.id_user IS NULL ) then 'Pendiente'

    Db.db.run(p)
  }



  def getVisitas(id: Long)(implicit obs: Seq[String]): Future[Seq[Visita]] = {
    val query = {
      for {
        v <- ventas.filter(x => x.id === id && x.idObraSocial.inSetBind(obs))
        vis <- visitas.filter(x => x.idVenta === v.id)
      } yield  vis
    }
    Db.db.run(query.result)
  }

  def getVisita(idVenta: Long)(implicit obs: Seq[String]): Future[Visita] = {

    Db.db.run(visitas.filter(_.idVenta === idVenta).sortBy(_.id.desc).result.head)
  }

  def all(user: String): Future[Seq[(Venta, Visita)]] = {
    val query = {
      for {
        e <- estados.filter(x => (x.estado === VISITA_CREADA || x.estado === VISITA_REPACTADA || x.estado === VISITA_CONFIRMADA || x.estado === RECHAZO_LOGISTICA) && x.user === user ).map(_.idVenta)
        v <- ventas.filter(x => x.id === e )
        vis <- visitas.filter(_.idVenta === v.id)
      } yield (v, vis)
    }

    Db.db.run(query.result)
  }




}
