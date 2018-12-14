package models

case class Auditoria(
                      id: Long,
                      idVenta: Long,
                      audio1: String,
                      adherentes: String,
                      audio2: Option[String] = None,
                      audio3: Option[String] = None,
                      observacion: Option[String] = None,
                    ) {

}
