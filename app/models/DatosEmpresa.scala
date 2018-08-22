package models

case class DatosEmpresa(
                         idVenta: Long,
                         empresa: Option[String] = None,
                         direccion: Option[String] = None,
                         localidad: Option[String] = None,
                         cantidadEmpleados: Option[String] = None,
                         horaEntrada: Option[String] = None,
                         horaSalida: Option[String] = None,
                       ) {

}
