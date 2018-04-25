package models

import akka.http.scaladsl.model.DateTime

case class Usuario(
                  user: String,
                  email: String,
                  password: String,
                  nombre: String,
                  borrado: Option[Boolean]
                  ) {

}
