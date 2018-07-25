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
  val RECHAZO_LOGISTICA = "Rechazo por logistica"
  val PRESENTADA = "Presentada"
  val PAGADA = "Pagada"
  val RECHAZO_ADMINISTRACION = "Rechazo por administracion"
  val RECHAZO_PRESENTACION = "Presentacion rechazada"

}
