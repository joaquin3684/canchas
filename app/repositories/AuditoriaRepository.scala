package repositories
import scala.concurrent.Future

import models._
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas.{estados, ventas, auditorias}

object AuditoriaRepository extends Estados {

  def ventasParaAuditar()(implicit obs: Seq[String]) : Future[Seq[Venta]]= {
    val query = for {
      e <- estados.filter(x => x.estado === VALIDADO && !(x.dni in estados.filter(x => x.estado === AUDITORIA_APROBADA || x.estado === AUDITORIA_OBSERVADA || x. estado === RECHAZO_AUDITORIA).map(_.dni)))
      v <- ventas.filter(x => x.dni === e.dni && x.idObraSocial.inSetBind(obs))
    } yield v
    Db.db.run(query.result)
  }

  def all(user: String) = {
    val query = for {
      e <- estados.filter(x => x.user === user && (x.estado === AUDITORIA_APROBADA || x.estado === AUDITORIA_OBSERVADA || x. estado === RECHAZO_AUDITORIA))
      v <- ventas.filter(x => x.dni === e.dni)
    } yield v
    Db.db.run(query.result)
  }

  def auditar(auditoria: Auditoria, estado: Estado) = {
    val e = estados += estado
    /*val audi = auditorias += auditoria
    val fullQuery = DBIO.seq(audi, e)
    */Db.db.run(e)
  }

}
