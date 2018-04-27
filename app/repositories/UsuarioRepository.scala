package repositories

import models._
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas.{usuarios, usuariosObrasSociales, obrasSociales, usuariosPerfiles, perfiles, pantallas, pantallasRutas, rutas, perfilesPantallas}

import scala.concurrent.Future

class UsuarioRepository {
  val db = Database.forConfig("db.default")

  def create(user: Usuario, obrasSociales: Seq[UsuarioObraSocial], perfiles: Seq[UsuarioPerfil]) = {

    val userNuevo = usuarios += user
    val obs = usuariosObrasSociales ++= obrasSociales
    val per = usuariosPerfiles ++= perfiles
    val fullQuery = DBIO.seq(userNuevo, obs, per)

    db.run(fullQuery.transactionally)
  }

  def getById(user: String)(implicit obs: Seq[String]): Future[Seq[(Usuario, ObraSocial, Perfil)]] = {

    val query = {
      for {
        u <- usuarios.filter(x => x.borrado.isEmpty && x.user === user)
        uo <- usuariosObrasSociales.filter( x => x.idObraSocial.inSetBind(obs) && x.idUsuario === user)
        o <- obrasSociales if o.nombre === uo.idObraSocial
        up <- usuariosPerfiles if up.idUsuario === user
        p <- perfiles if p.nombre === up.idPerfil
      } yield (u, o, p)
    }
    db.run(query.result)
  }

  def all()(implicit obs: Seq[String]) : Future[Seq[Usuario]] = {

    val query = {
      for {
        uo <- usuariosObrasSociales.filter(x => x.idObraSocial.inSetBind(obs))
        u <- usuarios.filter(x => x.borrado.isEmpty && x.user === uo.idUsuario)
      } yield u
    }
    db.run(query.result)
  }



  def checkObraSocial(user: String)(implicit obs: Seq[String]) : Future[Option[String]] = {

    val query = {
      for {
        uo <- usuariosObrasSociales.filter(x => x.idObraSocial.inSetBind(obs) && x.idUsuario === user)
      } yield uo.idUsuario
    }
    db.run(query.result.headOption)

  }

  def update(user: String, userNuevo: Usuario, perfiles: Seq[UsuarioPerfil],  obrasSociales: Seq[UsuarioObraSocial] ) =  {


    val userObsBorrados = usuariosObrasSociales.filter({ x => x.idUsuario === user}).delete
    val userPerBorrados = usuariosPerfiles.filter({ x => x.idUsuario === user}).delete

    val modUser = usuarios.filter(_.user === user).map( u => (u.email, u.nombre) ).update(userNuevo.email, userNuevo.nombre)
    val userObs = usuariosObrasSociales ++= obrasSociales
    val userPerfiles = usuariosPerfiles ++= perfiles

    val fullQuery = DBIO.seq(userObsBorrados, userPerBorrados, modUser, userObs, userPerfiles)

    db.run(fullQuery.transactionally)
  }

  def delete(user: String) = {
    db.run(usuarios.filter(_.user === user).map(_.borrado).update(Some(true)))
  }

  def validateCredentials(username: String): Future[Seq[(Usuario, ObraSocial, Pantalla)]] = {
    val q = {
      for {
          u <- usuarios.filter(_.user === username)
          uo <- usuariosObrasSociales.filter(_.idUsuario === u.user)
          o <- obrasSociales.filter(_.nombre === uo.idObraSocial)
          up <- usuariosPerfiles.filter(_.idUsuario === u.user)
          per <- perfiles.filter(_.nombre === up.idPerfil)
          pp <- perfilesPantallas.filter(_.idPerfil === per.nombre)
          p <- pantallas.filter(_.nombre === pp.idPantalla)
      } yield (u, o, p)
    }
    db.run(q.result)
  }

  def getRuta(path: String, panta: Seq[String]): Future[Option[PantallaRuta]] = {
    val q = {
      for {
        pr <- pantallasRutas.filter {pant => pant.idRuta === path && panta.contains(pant.idPantalla)}
      } yield pr
    }
    db.run(q.result.headOption)

  }

  def cambiarPassword(user: String, password: String) = {
    db.run(usuarios.filter(_.user === user).map(_.password).update(password))
  }
}
