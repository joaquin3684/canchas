package models
import java.sql.Timestamp
import java.time.{LocalDateTime, ZoneOffset}

import slick.jdbc.MySQLProfile.api._


abstract class BaseTable[E](tag: Tag, tableName: String)
  extends Table[E](tag, tableName) {

  implicit val localDateTimeMapping  = MappedColumnType.base[LocalDateTime, Timestamp](
    localDateTime => Timestamp.from(localDateTime.toInstant(ZoneOffset.UTC)),_.toLocalDateTime
  )
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def created_at = column[LocalDateTime]("created_at", O.Default(LocalDateTime.now()))
  def updated_at = column[Option[LocalDateTime]]("updated_at", O.Default(None))
  def deleted_at = column[Option[LocalDateTime]]("deleted_at", O.Default(None))

}
