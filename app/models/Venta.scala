package models

case class Venta(
                dni: Int,
                nombre: String,
                nacionalidad: String,
                domicilio: String,
                localidad: String,
                telefono: String,
                cuil: Int,
                estadoCivil: String,
                edad: Int,
                codem: Option[Boolean],
                superr: Option[Boolean],
                afip: Option[Boolean]
                ) {

}
