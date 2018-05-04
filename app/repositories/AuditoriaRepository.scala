package repositories
import scala.concurrent.Future

import models._
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas.{estados, ventas}

class AuditoriaRepository {

  def ventasParaAuditar()(implicit obs: Seq[String]) : Future[Seq[Venta]]= {
    val query = for {
      e <- estados.filter(x => x.estado === "Validado" && !(x.idVenta in estados.filter(x => x.estado === "Auditada" || x.estado === "Observada" || x. estado === "Rechazada por auditor").map(_.idVenta)))
      v <- ventas.filter(x => x.dni === e.idVenta && x.idObraSocial.inSetBind(obs))
    } yield v
    Db.db.run(query.result)
  }

  def all(user: String) = {
    val query = for {
      e <- estados.filter(x => x.user === user && (x.estado === "Auditada" || x.estado === "Observado" || x. estado === "Rechazada por auditor"))
      v <- ventas.filter(x => x.dni === e.idVenta)
    } yield v
    Db.db.run(query.result)
  }

}
