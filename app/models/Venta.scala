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

  def validar(datos: (Option[Boolean], Option[Boolean], Option[Boolean], Option[String], Option[String], Option[String]), user: String) : Estado = {
    val h = Seq(datos._1, datos._2, datos._3)
    val checkAprobacion = h.forall(_.get == true)
    if(checkAprobacion) Estado(user, dni, "Validado", DateTime.now) else Estado(user, dni, "Rechazo por validador", DateTime.now)

  }

}
