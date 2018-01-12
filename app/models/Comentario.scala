package models

import java.time.LocalDateTime

case class Comentario(id: Long, comentario: String, lugarId: Long, usuarioId: Long, created_at: LocalDateTime = LocalDateTime.now(), updated_at: Option[LocalDateTime] = None, deleted_at: Option[LocalDateTime] = None) extends BaseEntity {

}
