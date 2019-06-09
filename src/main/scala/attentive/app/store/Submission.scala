package attentive.app.store

import fs2._
import doobie._
import doobie.implicits._
import java.time._

case class Submission(
  id: Long
    , accountName: String
    , session: Option[String]
    , number: Int
    , title: String
    , artist: String
    , album: String
    , length: Int
    , mbid: String
    , submissionTime: Instant
    , created: Instant
)

object Submission {
  private val columnsNoId = fr"account_name,login_session,num,title,artist,album,len,mbid,submission_time,created"
  private val columns = sql"id," ++ columnsNoId

  def store(a: Submission): ConnectionIO[Int] =
    (sql"INSERT INTO Submission ("++ columnsNoId ++ fr") VALUES (${a.accountName}, ${a.session}, ${a.number}, ${a.title}, ${a.artist}, ${a.album}, ${a.length}, ${a.mbid}, ${a.submissionTime}, ${a.created})").update.run

  def find(account: String): Stream[ConnectionIO, Submission] =
    (fr"SELECT"++ columns ++ fr"FROM Submission WHERE account_name = $account ORDER BY submission_time DESC").
      query[Submission].stream

  def findAll: Stream[ConnectionIO, Submission] =
    (fr"SELECT" ++ columns ++ fr"FROM Submission").query[Submission].stream

  def countAll(account: Option[String]): ConnectionIO[Long] =
    (fr"SELECT count(*) from Submission" ++ whereAccount(account)).query[Long].unique

  def countArtists(account: Option[String]): ConnectionIO[Long] =
    (fr"SELECT count(distinct artist) FROM Submission" ++ whereAccount(account)).query[Long].unique

  // sqlite cannot do count with multiple columns
  def countAlbums(account: Option[String]): ConnectionIO[Long] =
    (fr"SELECT count(distinct album) FROM Submission" ++ whereAccount(account)).query[Long].unique

  private def whereAccount(a: Option[String]) =
    a.map(name => fr"WHERE account_name = $name").getOrElse(sql"")
}
