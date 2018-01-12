package schemas

import models._
import slick.jdbc.MySQLProfile.api._

object Schemas {

  class Lugares(tag: Tag) extends BaseTable[Lugar](tag, "lugares") {

    def nombre = column[String]("nombre")

    def domicilio = column[String]("domicilio")

    def telefono = column[Int]("telefono")

    def * = (id, nombre, domicilio, telefono, created_at, updated_at, deleted_at) <> (Lugar.tupled, Lugar.unapply)

  }

  val lugares = TableQuery[Lugares]

  class Canchas(tag: Tag) extends BaseTable[Cancha](tag, "canchas") {

    def nro = column[Int]("nro")

    def precio = column[Double]("precio")

    def suelo = column[String]("suelo")

    def lugarId = column[Long]("lugar_id")

    def lugar = foreignKey("fk_lugar", lugarId, lugares)(_.id)

    def * = (id, nro, precio, suelo, lugarId, created_at, updated_at, deleted_at) <> (Cancha.tupled, Cancha.unapply)
  }

  val canchas = TableQuery[Canchas]

  class Usuarios(tag: Tag) extends BaseTable[Usuario](tag, "usuarios") {

    def usuario = column[String]("usuario")

    def email = column[String]("email")

    def * = (id, usuario, email, created_at, updated_at, deleted_at) <> (Usuario.tupled, Usuario.unapply)

  }

  val usuarios = TableQuery[Usuarios]

  class Reservas(tag: Tag) extends BaseTable[Reserva](tag, "reservas") {

    def usuarioId = column[Long]("usuario_id")

    def usuario = foreignKey("fk_usuario", usuarioId, usuarios)(_.id)

    def canchaId = column[Long]("cancha_id")

    def cancha = foreignKey("fk_cancha", canchaId, canchas)(_.id)

    def * = (id, usuarioId, canchaId, created_at, updated_at, deleted_at) <> (Reserva.tupled, Reserva.unapply)
  }

  val reservas = TableQuery[Reservas]

  class Comentarios(tag: Tag) extends BaseTable[Comentario](tag, "comentarios") {

    def comentario = column[String]("comentario")

    def usuarioId = column[Long]("usuario_id")

    def usuario = foreignKey("fk_usuario", usuarioId, usuarios)(_.id)

    def lugarId = column[Long]("lugar_id")

    def lugar = foreignKey("fk_lugar", lugarId, lugares)(_.id)

    def * = (id, comentario, usuarioId, lugarId, created_at, updated_at, deleted_at) <> (Comentario.tupled, Comentario.unapply)

  }

  val comentarios = TableQuery[Comentarios]

  val allSchemas = lugares.schema ++ canchas.schema ++ usuarios.schema ++ reservas.schema ++ comentarios.schema
}