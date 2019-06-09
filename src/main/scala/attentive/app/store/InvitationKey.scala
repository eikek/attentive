package attentive.app.store

import fs2._
import cats.effect._
import doobie._
import doobie.implicits._
import java.time.{Duration => _, _}
import java.util.UUID
import scala.concurrent.duration.Duration

case class InvitationKey(
  key: String
    , created: Instant
)

object InvitationKey {
  private val columns = fr"invite,created"

  def store(a: InvitationKey): ConnectionIO[Int] =
    (sql"INSERT INTO InvitationKey ("++ columns ++ fr") VALUES (${a.key}, ${a.created})").update.run

  def delete(key: String): ConnectionIO[Int] =
    sql"DELETE FROM InvitationKey WHERE invite = $key".update.run

  def deleteOlder(pt: Instant): ConnectionIO[Int] =
    sql"DELETE FROM InvitationKey WHERE created < $pt".update.run

  def find(key: String): ConnectionIO[Option[InvitationKey]] =
    (fr"SELECT"++ columns ++ fr"FROM InvitationKey WHERE invite = $key").
      query[InvitationKey].option

  def use(key: String, valid: Duration): ConnectionIO[Boolean] = {
    for {
      kopt  <- find(key)
      _     <- delete(key)
    } yield kopt.exists(k => k.created.plusMillis(valid.toMillis).isAfter(Instant.now))
  }

  def exists(key: String): ConnectionIO[Boolean] =
    sql"SELECT count(*) FROM InvitationKey where invite = $key".
      query[Int].unique.map(_ > 0)

  def findAll: Stream[ConnectionIO, InvitationKey] =
    (fr"SELECT" ++ columns ++ fr"FROM InvitationKey").query[InvitationKey].stream


  def generate[F[_]: Sync]: F[InvitationKey] =
    Sync[F].delay {
      InvitationKey(UUID.randomUUID.toString, Instant.now)
    }

}
