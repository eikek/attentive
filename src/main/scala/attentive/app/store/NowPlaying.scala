package attentive.app.store

import fs2._
import doobie._
import doobie.implicits._
import java.time._

case class NowPlaying(
  accountName: String
    , session: Option[String]
    , number: Int
    , title: String
    , artist: String
    , album: String
    , length: Int
    , created: Instant
) {

  def isValid: Boolean =
    created.plusSeconds(length.toLong).isAfter(Instant.now)
}

object NowPlaying {
  private val columns = fr"account_name,login_session,num,title,artist,album,len,created"

  def store(a: NowPlaying): ConnectionIO[Int] =
    (sql"INSERT INTO NowPlaying ("++ columns ++ fr") VALUES (${a.accountName}, ${a.session}, ${a.number}, ${a.title}, ${a.artist}, ${a.album}, ${a.length}, ${a.created})").update.run

  def delete(account: String): ConnectionIO[Int] =
    sql"DELETE FROM NowPlaying WHERE account_name = $account".update.run

  def update(a: NowPlaying): ConnectionIO[Unit] =
    for {
      _  <- delete(a.accountName)
      _  <- store(a)
    } yield ()

  def find(account: String): ConnectionIO[Option[NowPlaying]] =
    (fr"SELECT"++ columns ++ fr"FROM NowPlaying WHERE account_name = $account").
      query[NowPlaying].option

  def exists(account: String): ConnectionIO[Boolean] =
    sql"SELECT count(*) FROM NowPlaying where account_name = $account".
      query[Int].unique.map(_ > 0)

  def findAll: Stream[ConnectionIO, NowPlaying] =
    (fr"SELECT" ++ columns ++ fr"FROM NowPlaying").query[NowPlaying].stream
}
