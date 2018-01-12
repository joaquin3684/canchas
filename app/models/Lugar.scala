package models

import java.time.LocalDateTime

case class Lugar(id: Long, nombre: String, domicilio: String, telefono: Int, created_at: LocalDateTime = LocalDateTime.now(), updated_at: Option[LocalDateTime] = None, deleted_at: Option[LocalDateTime] = None) extends BaseEntity {

}
