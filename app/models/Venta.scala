package models

import akka.http.scaladsl.model.DateTime

case class Venta(
                dni: Int,
                nombre: String,
                nacionalidad: String,
                domicilio: String,
                localidad: String,
                telefono: String,
                cuil: String,
                estadoCivil: String,
                edad: Int,
                idObraSocial: String,
                codem: Option[Boolean],
                superr: Option[Boolean],
                afip: Option[Boolean],
                motivoCodem: Option[String],
                motivoSuper: Option[String],
                motivoAfip: Option[String],
                ) {

  def validar(user: String) : Estado = {
    val h = Seq(codem, superr, afip)

    val checkAprobacion = h.forall(_ == true)
    if(checkAprobacion)
      Estado(user, dni, "Validado", DateTime.now)
    else
      Estado(user, dni, "Rechazo por validador", DateTime.now)

  }

}
