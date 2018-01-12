package repositories

import models.{Lugar}
import schemas.Schemas.Lugares
import slick.lifted.TableQuery

class LugarRepository extends BaseRepository[Lugares, Lugar](TableQuery[Lugares]) {

}
