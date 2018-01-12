package models

import java.time.LocalDateTime

case class Usuario(id: Long, usuario: String, email: String, created_at: LocalDateTime = LocalDateTime.now(), updated_at: Option[LocalDateTime] = None, deleted_at: Option[LocalDateTime] = None) extends BaseEntity {

}
