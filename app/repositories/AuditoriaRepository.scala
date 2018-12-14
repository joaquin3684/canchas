package repositories
import akka.http.scaladsl.model.DateTime

import scala.concurrent.Future
import models._
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas.{auditorias, datosEmpresas, estados, usuariosPerfiles, ventas}
import slick.dbio.{DBIOAction, NoStream}

object AuditoriaRepository extends Estados {

  def ventasParaAuditar()(implicit obs: Seq[String]) : Future[Seq[(Venta, Estado, String)]]= {
    val query = for {
      e <- estados.filter(x => x.estado === VALIDADO && !(x.idVenta in estados.filter(x => x.estado === AUDITORIA_APROBADA || x.estado === AUDITORIA_OBSERVADA || x.estado === RECHAZO_AUDITORIA).map(_.idVenta)))
      v <- ventas.filter(x => x.id === e.idVenta && x.idObraSocial.inSetBind(obs))
      e2 <- estados.filter(x => x.estado === CREADO && v.id === x.idVenta)
      up <- usuariosPerfiles.filter(x => x.idUsuario === e2.user)
    } yield (v, e2, up.idPerfil)
    Db.db.run(query.result)
  }

  def all(user: String) = {
    val query = for {
      e <- estados.filter(x => x.user === user && (x.estado === AUDITORIA_APROBADA || x.estado === AUDITORIA_OBSERVADA || x. estado === RECHAZO_AUDITORIA))
      v <- ventas.filter(x => x.id === e.idVenta)
    } yield v
    Db.db.run(query.result)
  }

  def auditar(auditoria: Auditoria, estado: Estado, datos:DatosEmpresa, modificar: Boolean, capitas: Int) = {
    val e = estados += estado
    val audi = auditorias += auditoria
    if(modificar) {
      val d = datosEmpresas.filter(_.idVenta === datos.idVenta).update(datos)
      if(capitas != 99){
        val v = ventas.filter(x => x.id === auditoria.idVenta).map(_.capitas).update(Some(capitas))
        val fullQuery = DBIO.seq(audi, e, d, v)
        Db.db.run(fullQuery)

      } else{
        val fullQuery = DBIO.seq(audi, e, d)
        Db.db.run(fullQuery)

      }

    }
    else {
      if(capitas != 99){
        val v = ventas.filter(x => x.id === auditoria.idVenta).map(_.capitas).update(Some(capitas))
        val fullQuery = DBIO.seq(audi, e, v)
        Db.db.run(fullQuery)

      } else{
        val fullQuery = DBIO.seq(audi, e)
        Db.db.run(fullQuery)

      }
    }
  }

}
