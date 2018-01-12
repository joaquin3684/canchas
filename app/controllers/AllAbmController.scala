package controllers

import javax.inject.{Singleton}

import models.{BaseEntity, BaseTable}
import play.api.mvc._
import repositories.{BaseRepository}


import scala.concurrent.Await
import scala.concurrent.duration.Duration

@Singleton
abstract class AllAbmController[E <: BaseEntity,A <: BaseTable[E], T <: BaseRepository[A, E]](cc: ControllerComponents) extends AbstractController(cc) {

  val repo: T
  def create(row: E) = Action {
    val creado = repo.create(row)
    Await.ready(creado, Duration.Inf)
    Ok("hola")
  }

  def getById(id: Long) = Action {
    val elem = repo.getById(id)
    Await.ready(elem, Duration.Inf)
    Ok("piola")
  }

  def delete(id: Long) = Action {
    val elem = repo.delete(id)
    Await.ready(elem, Duration.Inf)
    Ok("pim")
  }

  def all() = Action {
    val all = repo.getAll
    Await.ready(all, Duration.Inf)
    Ok("pim")
  }

  def update(id: Long, row: E) = Action {
    val newElem = repo.update(id, row)
    Await.ready(newElem, Duration.Inf)
    Ok("pim")
  }
}
