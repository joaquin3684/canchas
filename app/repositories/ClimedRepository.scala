package repositories

import models._
import schemas.Schemas.{obrasSociales}

import scala.concurrent.{Await, Future}
import slick.jdbc.MySQLProfile.api._


class ClimedRepository {

 /* val db = Database.forConfig("db.default")

  def create(climed: Climed) = {

    val climedNuevo = (climeds returning climeds.map(_.id)) into ((clim,id) => clim.copy(id = id)) += climed
    db.run(climedNuevo)

  }


  def createRelationships(climedEsps: Seq[ClimedEspecialidad], climedObs: Seq[ClimedObraSocial]) = {
    val climedsEspAgregados = climedsEspecialidades ++= climedEsps
    val climedsObsAgregados = climedsObrasSociales ++= climedObs

    val fullQuery = DBIO.seq(climedsEspAgregados, climedsObsAgregados)

    db.run(fullQuery.transactionally)

  }

  def getById(id: Long): Future[Seq[(Climed, Especialidad, ObraSocial)]] = {
    val query = {
     for {
        c <- climeds if c.id === id
        ce <- climedsEspecialidades if ce.idClimed === id
        co <- climedsObrasSociales if ce.idClimed === id
        e <- especialidades if ce.idEspecialidad === e.id
        o <- obrasSociales if co.idObraSocial === o.id
      } yield (c, e, o)

    }
    db.run(query.result)
  }

  def update(id: Long, climed: Climed, climedEsps: Seq[ClimedEspecialidad], climedObs: Seq[ClimedObraSocial]) =  {
    val climedsEspBorrados =  climedEsps.map(row => {
      climedsEspecialidades.filter({ cliesp => cliesp.idClimed === id})
        .delete
    })
    val climedsEspAgregados = climedsEspecialidades ++= climedEsps
    val dbioClimedsEspBorrados = DBIO.sequence(climedsEspBorrados)

    val climedsObsBorrados =  climedObs.map(row => {
      climedsObrasSociales.filter({ cliobs => cliobs.idClimed === id})
        .delete
    })
    val climedsObsAgregados = climedsObrasSociales ++= climedObs
    val dbioClimedsObsBorrados = DBIO.sequence(climedsObsBorrados)

    val modClimed = climeds.filter(_.id === id).update(climed)

    val fullQuery = DBIO.seq(dbioClimedsEspBorrados, climedsEspAgregados, dbioClimedsObsBorrados, climedsObsAgregados, modClimed)

    db.run(fullQuery.transactionally)
  }

  def delete(id: Long) = {
    db.run(climeds.filter(_.id === id).delete)
  }

  def all(): Future[Seq[Climed]] = {
    db.run(climeds.result)
  }*/

}
