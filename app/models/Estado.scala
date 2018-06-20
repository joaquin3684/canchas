package models

import akka.http.scaladsl.model.DateTime

case class Estado(
                 user: String,
                 dni: Int,
                 estado: String,
                 fecha: DateTime,
                 observacion: Option[String] = None,
                 id: Long = 0
                 ) {

}
