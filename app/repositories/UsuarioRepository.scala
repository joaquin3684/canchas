package repositories

import models._
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas.{obrasSociales, pantallas, pantallasRutas, perfiles, perfilesPantallas, rutas, usuarios, usuariosObrasSociales, usuariosPerfiles}

import scala.concurrent.Future

object UsuarioRepository {

  def create(user: Usuario, obrasSociales: Seq[UsuarioObraSocial], perfiles: Seq[UsuarioPerfil]) = {

    val userNuevo = usuarios += user
    val obs = usuariosObrasSociales ++= obrasSociales
    val per = usuariosPerfiles ++= perfiles
    val fullQuery = DBIO.seq(userNuevo, obs, per)

    Db.db.run(fullQuery.transactionally)
  }

  def getById(user: String)(implicit obs: Seq[String]): Future[Seq[(Usuario, ObraSocial, Perfil)]] = {

    val query = {
      for {
        u <- usuarios.filter(x => x.borrado === false && x.user === user)
        uo <- usuariosObrasSociales.filter( x => x.idObraSocial.inSetBind(obs) && x.idUsuario === user)
        o <- obrasSociales if o.nombre === uo.idObraSocial
        up <- usuariosPerfiles if up.idUsuario === user
        p <- perfiles if p.nombre === up.idPerfil
      } yield (u, o, p)
    }
    Db.db.run(query.result)
  }

  def all()(implicit obs: Seq[String]) : Future[Seq[Usuario]] = {

    val query = {
      for {
        uo <- usuariosObrasSociales.filter(x => x.idObraSocial.inSetBind(obs))
        u <- usuarios.filter(x => x.borrado === false && x.user === uo.idUsuario)
      } yield u
    }
    Db.db.run(query.result)
  }



  def checkObraSocial(user: String)(implicit obs: Seq[String]) : Future[Option[String]] = {

    val query = {
      for {
        uo <- usuariosObrasSociales.filter(x => x.idObraSocial.inSetBind(obs) && x.idUsuario === user)
      } yield uo.idUsuario
    }
    Db.db.run(query.result.headOption)

  }

  def update(user: String, userNuevo: Usuario, perfiles: Seq[UsuarioPerfil],  obrasSociales: Seq[UsuarioObraSocial] ) =  {


    val userObsBorrados = usuariosObrasSociales.filter({ x => x.idUsuario === user}).delete
    val userPerBorrados = usuariosPerfiles.filter({ x => x.idUsuario === user}).delete

    val modUser = usuarios.filter(_.user === user).map( u => (u.user, u.email, u.nombre) ).update(userNuevo.user, userNuevo.email, userNuevo.nombre)
    val userObs = usuariosObrasSociales ++= obrasSociales
    val userPerfiles = usuariosPerfiles ++= perfiles

    val fullQuery = DBIO.seq(userObsBorrados, userPerBorrados, modUser, userObs, userPerfiles)

    Db.db.run(fullQuery.transactionally)
  }

  def delete(user: String) = {
    Db.db.run(usuarios.filter(_.user === user).map(_.borrado).update(true))
  }

  def validateCredentials(username: String): Future[Seq[(Usuario, ObraSocial, Pantalla, Perfil)]] = {
    val q = {
      for {
          u <- usuarios.filter(x => x.user === username && x.borrado === false)
          uo <- usuariosObrasSociales.filter(_.idUsuario === u.user)
          o <- obrasSociales.filter(_.nombre === uo.idObraSocial)
          up <- usuariosPerfiles.filter(_.idUsuario === u.user)
          per <- perfiles.filter(_.nombre === up.idPerfil)
          pp <- perfilesPantallas.filter(_.idPerfil === per.nombre)
          p <- pantallas.filter(_.nombre === pp.idPantalla)
      } yield (u, o, p, per)
    }
    Db.db.run(q.result)
  }

  def getRuta(path: String, panta: Seq[String]): Future[Option[PantallaRuta]] = {
    val q = {
      for {
        pr <- pantallasRutas.filter {pant => pant.idRuta === path && panta.contains(pant.idPantalla)}
      } yield pr
    }
    Db.db.run(q.result.headOption)

  }

  def cambiarPassword(user: String, password: String) = {
    Db.db.run(usuarios.filter(_.user === user).map(_.password).update(password))
  }

  def getPerfiles : Future[Seq[Perfil]] = {
    Db.db.run(perfiles.result)
  }

  def usuariosLogistica : Future[Seq[Usuario]] = {
    val q = for {
      up <- usuariosPerfiles.filter(x => x.idPerfil === "vendedora" || x.idPerfil === "promotora" || x.idPerfil === "cadete")
      u <- usuarios.filter(x => x.user === up.idUsuario && x.borrado === false)

    } yield u

    Db.db.run(q.result)
  }

  def usuariosCreacion : Future[Seq[Usuario]] = {
    val q = for {
      up <- usuariosPerfiles.filter(x => x.idPerfil === "vendedora" || x.idPerfil === "promotora" || x.idPerfil === "externo")
      u <- usuarios.filter(x => x.user === up.idUsuario && x.borrado === false)

    } yield u

    Db.db.run(q.result)
  }


}
