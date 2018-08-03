package models

import akka.http.scaladsl.model.DateTime
case class Validacion (
                       idVenta: Long,
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
      Estado(user, idVenta, "Validado", DateTime.now)
     else {
       val motivo = Seq(motivoAfip, motivoCodem, motivoSuper).filter(_.isDefined).map(_.get).mkString(" + ")
       if(codem)
         Estado(user, idVenta,"Rechazo por validacion" , DateTime.now, true, Some(motivo))
       else
         Estado(user, idVenta, "Rechazo por validacion", DateTime.now, false, Some(motivo))
     }

  }
}
