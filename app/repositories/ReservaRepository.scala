package repositories

import models.Reserva
import schemas.Schemas.Reservas
import slick.lifted.TableQuery

class ReservaRepository extends BaseRepository[Reservas, Reserva](TableQuery[Reservas]){

}
