package repositories

import models.Usuario
import schemas.Schemas.Usuarios
import slick.lifted.TableQuery

class UsuarioRepository extends BaseRepository[Usuarios, Usuario](TableQuery[Usuarios]){

}
