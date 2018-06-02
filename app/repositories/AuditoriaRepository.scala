package repositories
import scala.concurrent.Future

import models._
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas.{estados, ventas}

class AuditoriaRepository {

  def ventasParaAuditar()(implicit obs: Seq[String]) : Future[Seq[Venta]]= {
    val query = for {
      e <- estados.filter(x => x.estado === "Validado" && !(x.dni in estados.filter(x => x.estado === "Auditoria aprobada" || x.estado === "Observada" || x. estado === "Rechazada por auditor").map(_.dni)))
      v <- ventas.filter(x => x.dni === e.dni && x.idObraSocial.inSetBind(obs))
    } yield v
    Db.db.run(query.result)
  }

  def all(user: String) = {
    val query = for {
      e <- estados.filter(x => x.user === user && (x.estado === "Auditoria aprobada" || x.estado === "Observada" || x. estado === "Rechazada por auditor"))
      v <- ventas.filter(x => x.dni === e.dni)
    } yield v
    Db.db.run(query.result)
  }

  def auditar(estado: Estado, dni: Int, observacion: Option[String], audio: Option[String]) = {
    val e = estados += estado
    val update = ventas.filter(_.dni === dni).map(x => (x.audio, x.motivoAuditoria)).update(audio, observacion)
    val fullQuery = DBIO.seq(update, e)
    Db.db.run(fullQuery)
  }

}
