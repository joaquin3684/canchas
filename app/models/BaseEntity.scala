package models

import java.time.LocalDateTime

trait BaseEntity {
  val id: Long
  val created_at: LocalDateTime
  val updated_at: Option[LocalDateTime]
  val deleted_at: Option[LocalDateTime]

}
