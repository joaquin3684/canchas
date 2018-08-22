package models

import akka.http.scaladsl.model.DateTime

case class Venta(
                dni: Int,
                nombre: String,
                nacionalidad: String,
                domicilio: String,
                localidad: String,
                telefono: String,
                cuil: String = "",
                estadoCivil: String,
                edad: Int,
                idObraSocial: String,
                fechaNacimiento: Option[DateTime],
                zona: String,
                codigoPostal: Int,
                horaContactoTel: String,
                piso: Option[String],
                dpto: Option[String],
                celular: Option[String],
                horaContactoCel: Option[String],
                base: Option[String],
                empresa: Option[String] = None,
                cuit: Option[String] = None,
                tresPorciento: Option[Double] = None,
                id: Long = 0
                ) {


}
