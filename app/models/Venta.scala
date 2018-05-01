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

  def validar(datos: (Option[Boolean], Option[Boolean], Option[Boolean], Option[String], Option[String], Option[String]), user: String) : (Venta, Estado) = {
    val h = Seq(datos._1, datos._2, datos._3)

    val checkAprobacion = h.forall(_.get == true)
    val estado = if(checkAprobacion) Estado(user, dni, "Validado", DateTime.now) else Estado(user, dni, "Rechazo por validador", DateTime.now)
    val venta = this.copy(codem = datos._1, superr = datos._2, afip = datos._3, motivoCodem = datos._4, motivoSuper = datos._5, motivoAfip = datos._6)
    (venta, estado)
  }

}
