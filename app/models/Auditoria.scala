package models

case class Auditoria(
                      idVenta: Long,
                      audio: String,
                      capitas: Int,
                      audio2: Option[String] = None,
                      audio3: Option[String] = None,
                      observacion: Option[String] = None,
                      empresa: Option[String] = None,
                      direccion: Option[String] = None,
                      localidad: Option[String] = None,
                      cantidadEmpleados: Option[String] = None,
                      horaEntrada: Option[String] = None,
                      horaSalida: Option[String] = None,
                    ) {

}
