package models

case class Auditoria(
                      idVenta: Long,
                      audio: String,
                      observacion: Option[String],
                      empresa: Option[String],
                      direccion: Option[String],
                      localidad: Option[String],
                      cantidadEmpleados: Option[String],
                      horaEntrada: Option[String],
                      horaSalida: Option[String],
                    ) {

}
