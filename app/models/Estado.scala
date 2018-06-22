package models

import akka.http.scaladsl.model.DateTime

case class Estado(
                 user: String,
                 dni: Int,
                 estado: String,
                 fecha: DateTime,
                 recuperable: Boolean = false,
                 observacion: Option[String] = None,
                 id: Long = 0
                 ) {

}
