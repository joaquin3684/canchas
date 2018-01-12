package repositories

import models.Cancha
import schemas.Schemas.Canchas
import slick.lifted.TableQuery

class CanchaRepository extends BaseRepository[Canchas, Cancha](TableQuery[Canchas]){

}
