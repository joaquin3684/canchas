package repositories


import slick.jdbc.MySQLProfile.api._


object Db {

  val db = Database.forConfig("db.default")
}
