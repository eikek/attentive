package attentive.app.store

import fs2._
import doobie._
import doobie.implicits._
import java.time._

case class Account(
  name: String
    , active: Boolean
    , password: String
    , created: Instant
)

object Account {
  private val columns = fr"account_name,active,password,created"

  def store(a: Account): ConnectionIO[Int] =
    (sql"INSERT INTO Account ("++ columns ++ fr") VALUES (${a.name}, ${a.active}, ${a.password}, ${a.created})").update.run

  def update(a: Account): ConnectionIO[Int] =
    sql"UPDATE Account SET active = ${a.active}, password = ${a.password}, created = ${a.created} WHERE account_name = ${a.name}".update.run

  def findActive(name: String): ConnectionIO[Option[Account]] =
    (fr"SELECT"++ columns ++ fr"FROM Account WHERE account_name = $name AND active = TRUE").
      query[Account].option

  def find(name: String): ConnectionIO[Option[Account]] =
    (sql"SELECT"++ columns ++ fr"FROM Account WHERE account_name = $name").
      query[Account].option

  def exists(name: String): ConnectionIO[Boolean] =
    sql"SELECT count(*) FROM Account where account_name = $name".
      query[Int].unique.map(_ > 0)

  def findAll: Stream[ConnectionIO, Account] =
    (sql"SELECT" ++ columns ++ fr"FROM Account ORDER BY account_name").query[Account].stream

  def count: ConnectionIO[Int] =
    sql"SELECT count(*) FROM Account".query[Int].unique
}
