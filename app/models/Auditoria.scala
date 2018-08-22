package models

case class Auditoria(
                      idVenta: Long,
                      audio1: String,
                      capitas: Int,
                      audio2: Option[String] = None,
                      audio3: Option[String] = None,
                      observacion: Option[String] = None,
                    ) {

}
