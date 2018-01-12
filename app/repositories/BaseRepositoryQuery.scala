package repositories
import java.sql.Timestamp
import java.time.{LocalDateTime, ZoneOffset}

import slick.jdbc.MySQLProfile.api._
import models.{BaseEntity, BaseTable}
import slick.lifted.CanBeQueryCondition

trait BaseRepositoryQuery [T <: BaseTable[E], E <: BaseEntity]{

  val query: TableQuery[T]

  implicit val localDateTimeMapping  = MappedColumnType.base[LocalDateTime, Timestamp](
    localDateTime => Timestamp.from(localDateTime.toInstant(ZoneOffset.UTC)),_.toLocalDateTime
  )

  def getByIdQuery(id: Long) = {
    query.filter(_.id === id).filter(_.deleted_at.isEmpty)
  }

  def getAllQuery = {
    query.filter(_.deleted_at.isEmpty)
  }

  def filterQuery[C <: Rep[_]](expr: T => C)(implicit wt: CanBeQueryCondition[C]) = {
    query.filter(expr).filter(_.deleted_at.isEmpty)
  }

  def saveQuery(row: E) = {
    query += row
  }

  def updateQuery(row: E) = {
    query.filter(_.id === row.id).filter(_.deleted_at.isEmpty).update(row)
  }

  def updateTimestamp(id:Long) = {
    query.filter(_.id === id).map(_.updated_at).update(Some(LocalDateTime.now()))
  }


  def deleteByIdQuery(id: Long) = {
    query.filter(_.id === id).map(_.deleted_at).update(Some(LocalDateTime.now()))
  }


}
