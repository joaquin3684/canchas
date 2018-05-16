package schemas

import java.sql.Timestamp
import java.time.{Instant, ZoneOffset}

import akka.http.scaladsl.model.DateTime
import models.{UsuarioPerfil, _}
import slick.jdbc.MySQLProfile.api._

object Schemas {


  implicit val localDateTimeMapping  = MappedColumnType.base[DateTime, Timestamp](
    dt => new Timestamp(dt.clicks),
    ts => DateTime(ts.getTime)
  )


  class ObrasSociales(tag: Tag) extends Table[ObraSocial](tag, "obras_sociales") {


    def nombre = column[String]("nombre", O.PrimaryKey)

    def * = nombre <> (ObraSocial.apply, ObraSocial.unapply)
  }

  val obrasSociales = TableQuery[ObrasSociales]


  class Usuarios(tag: Tag) extends  Table[Usuario](tag, "usuarios") {

    def user = column[String]("user", O.PrimaryKey)
    def email = column[String]("email")
    def password = column[String]("password")
    def nombre = column[String]("nombre")
    def borrado = column[Option[Boolean]]("borrado")

    def * = (user, email, password, nombre, borrado) <> (Usuario.tupled, Usuario.unapply)
  }

  val usuarios = TableQuery[Usuarios]

  class UsuariosObrasSociales(tag: Tag) extends Table[UsuarioObraSocial](tag, "usuario_obra_social") {

    def idUsuario = column[String]("id_usuario", O.Length(50))

    def idObraSocial = column[String]("id_obra_social", O.Length(50))

    def pk = primaryKey("pkusuarioobrasocial", (idUsuario, idObraSocial))

    def idUsuarioFk = foreignKey("fk_usuario_obra_social", idUsuario, usuarios)(_.user)

    def idObraSocialFk = foreignKey("fk_obra_social_usuario", idObraSocial, obrasSociales)(_.nombre)

    def * = (idUsuario, idObraSocial) <> (UsuarioObraSocial.tupled, UsuarioObraSocial.unapply)

  }

  val usuariosObrasSociales = TableQuery[UsuariosObrasSociales]


  class Perfiles(tag: Tag) extends Table[Perfil](tag, "perfiles") {

    def nombre = column[String]("nombre", O.PrimaryKey)

    def * = nombre <> (Perfil.apply, Perfil.unapply)
  }

  val perfiles = TableQuery[Perfiles]

  class UsuariosPerfiles(tag: Tag) extends Table[UsuarioPerfil](tag, "usuario_perfil") {

    def idUsuario = column[String]("user", O.Length(50))
    def idPerfil = column[String]("perfil", O.Length(50))

    def pk = primaryKey("pkusuarioperfiles", (idUsuario, idPerfil))

    def idUsuarioFk = foreignKey("fk_usuario_perfil", idUsuario, usuarios)(_.user)

    def idPerfilFk = foreignKey("fk_perfi_usuario", idPerfil, perfiles)(_.nombre)

    def * = (idUsuario, idPerfil) <> (UsuarioPerfil.tupled, UsuarioPerfil.unapply)
  }

  val usuariosPerfiles = TableQuery[UsuariosPerfiles]

  class Pantallas(tag: Tag) extends Table[Pantalla](tag, "pantallas") {

    def nombre = column[String]("nombre", O.PrimaryKey)

    def * = nombre <> (Pantalla.apply, Pantalla.unapply)
  }

  val pantallas = TableQuery[Pantallas]

  class Rutas(tag: Tag) extends Table[Ruta](tag, "rutas") {

    def path = column[String]("path", O.PrimaryKey)

    def * = path <> (Ruta.apply, Ruta.unapply)
  }

  val rutas = TableQuery[Rutas]

  class PerfilesPantallas(tag: Tag) extends Table[PerfilPantalla](tag, "perfil_pantalla") {

    def idPerfil = column[String]("id_perfil", O.Length(50))

    def idPantalla = column[String]("id_pantalla", O.Length(50))

    def pk = primaryKey("pkperfilpantalla", (idPerfil, idPantalla))

    def idPerfilFk = foreignKey("fk_perfil_pantalla", idPerfil, perfiles)(_.nombre)

    def idPantallaFk = foreignKey("fk_pantalla_perfil", idPantalla, pantallas)(_.nombre)

    def * = (idPerfil, idPantalla) <> (PerfilPantalla.tupled, PerfilPantalla.unapply)

  }

  val perfilesPantallas = TableQuery[PerfilesPantallas]

  class PantallasRutas(tag: Tag) extends Table[PantallaRuta](tag, "pantalla_ruta") {

    def idPantalla = column[String]("id_pantalla", O.Length(50))

    def idRuta = column[String]("id_ruta", O.Length(50))

    def pk = primaryKey("pkpantallasrutas", (idPantalla, idRuta))

    def idUsuarioFk = foreignKey("fk_pantalla_ruta", idPantalla, pantallas)(_.nombre)

    def idRutaFk = foreignKey("fk_ruta_pantalla", idRuta, rutas)(_.path)

    def * = (idPantalla, idRuta) <> (PantallaRuta.tupled, PantallaRuta.unapply)

  }

  val pantallasRutas = TableQuery[PantallasRutas]

  class Ventas(tag:Tag) extends Table[Venta](tag, "ventas") {

    def dni = column[Int]("dni", O.PrimaryKey)
    def nombre = column[String]("nombre")
    def nacionalidad = column[String]("nacionalidad")
    def domicilio = column[String]("domicilio")
    def localidad = column[String]("localidad")
    def telefono = column[String]("telefono")
    def cuil = column[String]("cuil")
    def estadoCivil = column[String]("estadoCivil")
    def edad = column[Int]("edad")
    def idObraSocial = column[String]("id_obra_social", O.Length(50))
    def obsFk = foreignKey("fk_venta_obs", idObraSocial, obrasSociales)(_.nombre)
    def codem = column[Option[Boolean]]("codem", O.Default(None))
    def superr = column[Option[Boolean]]("super", O.Default(None))
    def afip = column[Option[Boolean]]("afip", O.Default(None))
    def motivoCodem = column[Option[String]]("motivo_codem", O.Default(None))
    def motivoSuper = column[Option[String]]("motivo_super", O.Default(None))
    def motivoAfip = column[Option[String]]("motivo_afip", O.Default(None))
    def motivoAuditoria = column[Option[String]]("motivo_auditoria", O.Default(None))
    def audio = column[Option[String]]("audio", O.Default(None))

    def * = (dni, nombre, nacionalidad, domicilio, localidad, telefono, cuil, estadoCivil, edad, idObraSocial, codem, superr, afip, motivoCodem, motivoSuper, motivoAfip, motivoAuditoria, audio) <> (Venta.tupled, Venta.unapply)

  }

  val ventas = TableQuery[Ventas]

  class Estados(tag:Tag) extends Table[Estado](tag, "estados") {

    def user = column[String]("user", O.Length(50))
    def idVenta = column[Int]("id_venta")
    def estado = column[String]("estado", O.Length(50))
    def fecha = column[DateTime]("fecha", O.Default(DateTime.now))

    def pk = primaryKey("pkestados", (user, idVenta, estado))

    def userFk = foreignKey("fk_user_estado", user, usuarios)(_.user)

    def ventaFk = foreignKey("fk_venta_estado", idVenta, ventas)(_.dni)

    def * = (user, idVenta, estado, fecha) <> (Estado.tupled, Estado.unapply)
  }

  val estados = TableQuery[Estados]

  class Visitas(tag: Tag) extends Table[Visita](tag, "visitas") {

     def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
     def idVenta = column[Int]("id_venta")
     def idUser = column[String]("id_user", O.Length(50))
     def lugar= column[String]("lugar")
     def direccion= column[String]("direccion")
     def entreCalles= column[String]("entre_calles")
     def localidad= column[String]("localidad")
     def observacion= column[String]("observacion")
     def fecha= column[DateTime]("fecha")
     def estado= column[String]("estado")

     def ventaFk = foreignKey("fk_venta_visita", idVenta, ventas)(_.dni)
     def userFk = foreignKey("fk_user_visita", idUser, usuarios)(_.user)



    def * = (id, idVenta, idUser, lugar, direccion, entreCalles, localidad, observacion, fecha, estado) <> (Visita.tupled, Visita.unapply)
  }

  val visitas = TableQuery[Visitas]

  val allSchemas = {
      obrasSociales.schema ++
      usuarios.schema ++
      usuariosObrasSociales.schema ++
      perfiles.schema ++
      pantallas.schema ++
      rutas.schema ++
      perfilesPantallas.schema ++
      pantallasRutas.schema ++
      ventas.schema ++
      estados.schema ++
      usuariosPerfiles.schema ++
      visitas.schema
  }
}