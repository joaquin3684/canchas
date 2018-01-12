package models

import java.time.LocalDateTime

case class Reserva(id: Long, usuarioId: Long, canchaId: Long, created_at: LocalDateTime = LocalDateTime.now(), updated_at: Option[LocalDateTime] = None, deleted_at: Option[LocalDateTime] = None) extends BaseEntity {

}
