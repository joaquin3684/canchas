package repositories
import java.sql.Timestamp

import akka.http.scaladsl.model.DateTime

import scala.concurrent.Future
import models._
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas.{auditorias, estados, usuarios, usuariosPerfiles, validaciones, ventas}

object EstadisticaRepository extends Estados {


/*  def estadisticaGeneral() = {


    val p = sql"""select ventas.dni, ventas.nombre, ventas.cuil, ventas.telefono, ventas.nacionalidad, ventas.domicilio, ventas.localidad, ventas.estadoCivil, ventas.edad, ventas.id_obra_social, ventas.fecha_nacimiento, ventas.zona, ventas.codigo_postal, ventas.hora_contacto_tel, ventas.piso, ventas.departamento, ventas.celular, ventas.hora_contacto_cel, ventas.base
                                horaContactoTel,
                                from ventas
                         join estados on ventas.dni = estados.id_venta

      """.as[(Venta, String)]

  }*/
}