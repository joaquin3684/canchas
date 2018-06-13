package models

import akka.http.scaladsl.model.DateTime

case class Validacion(
                       dni: Int,
                       codem: Boolean,
                       superr: Boolean,
                       afip: Boolean,
                       capitas: Int,
                       motivoCodem: Option[String],
                       motivoSuper: Option[String],
                       motivoAfip: Option[String],
                     ) {

  def validar(user: String) : Estado = {
    val h = Seq(codem, superr, afip)

     if(h.forall(_ == true))
      Estado(user, dni, "Validado", DateTime.now)
    else
      Estado(user, dni, "Rechazo por validador", DateTime.now)

  }
}
