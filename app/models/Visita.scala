package models

import akka.http.scaladsl.model.DateTime

case class Visita(
                 id: Long,
                 dni: Int,
                 user: String,
                 lugar: String,
                 direccion: String,
                 entreCalles: String,
                 localidad: String,
                 observacion: Option[String],
                 fecha: DateTime,
                 hora: String,
                 estado: String
                 ) {

}
