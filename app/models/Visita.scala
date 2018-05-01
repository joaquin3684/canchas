package models

import akka.http.scaladsl.model.DateTime

case class Visita(
                 id: Long,
                 idVenta: Int,
                 idUser: String,
                 lugar: String,
                 direccion: String,
                 entreCalles: String,
                 localidad: String,
                 observacion: String,
                 fecha: DateTime,
                 estado: String
                 ) {

}
