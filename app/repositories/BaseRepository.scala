package repositories
import java.time.LocalDateTime

import models.{BaseEntity, BaseTable}
import slick.jdbc.MySQLProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import slick.jdbc.MySQLProfile.api._
import slick.lifted.CanBeQueryCondition

abstract class BaseRepository[T <: BaseTable[E], E <: BaseEntity](clazz: TableQuery[T]) extends BaseRepositoryQuery [T,E]{

  val classTable: TableQuery[T] = clazz
  val query: MySQLProfile.api.type#TableQuery[T] = clazz
  val db = Database.forConfig("db.default")

  def getAll: Future[Seq[E]] = {
    db.run(getAllQuery.result)
  }

  def getById(id: Long): Future[Option[E]] = {
    db.run(getByIdQuery(id).result.headOption)
  }

  def filter[C <: Rep[_]](expr: T => C)(implicit wt: CanBeQueryCondition[C]) = {
    db.run(filterQuery(expr).result)
  }

  def create(row: E) = {
    db.run(saveQuery(row))
  }

  def update(id: Long, row: E) = {
    db.run(updateQuery(row))
    db.run(updateTimestamp(id))
  }

  def delete(id: Long) = {
    db.run(deleteByIdQuery(id))
  }
}

