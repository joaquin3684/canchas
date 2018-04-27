package models

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

}
