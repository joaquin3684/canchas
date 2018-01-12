package repositories

import models.Comentario
import schemas.Schemas.Comentarios
import slick.lifted.TableQuery

class ComentarioRepository extends BaseRepository[Comentarios, Comentario](TableQuery[Comentarios]){

}
