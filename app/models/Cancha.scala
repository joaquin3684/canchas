package models

import java.time.LocalDateTime

case class Cancha(id: Long, nro: Int, precio: Double, suelo: String, lugarId: Long, created_at: LocalDateTime = LocalDateTime.now(), updated_at: Option[LocalDateTime] = None, deleted_at: Option[LocalDateTime] = None) extends BaseEntity {

}
