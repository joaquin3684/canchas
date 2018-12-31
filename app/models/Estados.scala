package models

trait Estados {

  val CREADO = "Creado"
  val VALIDADO = "Validado"
  val RECHAZO_VALIDACION = "Rechazo por validacion"
  val AUDITORIA_APROBADA = "Auditoria aprobada"
  val AUDITORIA_OBSERVADA = "Auditoria observada"
  val RECHAZO_AUDITORIA = "Rechazo por auditoria"
  val VISITA_CREADA = "Visita creada"
  val VISITA_REPACTADA = "Visita repactada"
  val VISITA_CONFIRMADA = "Visita confirmada"
  val PENDIENTE_DOC = "Pendiente de documentacion"
  val RECHAZO_LOGISTICA = "Rechazo por logistica"
  val PRESENTADA = "Presentada"
  val PAGADA = "Pagada"
  val RECHAZO_ADMINISTRACION = "Rechazo por administracion"
  val RECHAZO_PRESENTACION = "Rechazo presentacion"
  val DIGITALIZADA = "Venta digitalizada"

}
