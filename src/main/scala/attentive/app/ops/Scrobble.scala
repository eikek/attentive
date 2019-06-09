package attentive.app.ops

import cats.effect._
import cats.implicits._
import cats.Traverse
import doobie._
import doobie.implicits._
import java.time._

import attentive.app.store._

object Scrobble {

  sealed trait Result
  object Result {
    case object Ok extends Result

    def ok: Result = Ok
  }

  def submitTrack[F[_]: Effect](xa: Transactor[F])(account: String, songs: SubmissionInfo): F[Result] = {
    val subs = songs.map(si => Submission(
      0L, account, None,
      si.number, si.title, si.artist, si.album, si.length, si.mbid,
      si.timeSeconds.map(s => Instant.ofEpochSecond(s.toLong)).getOrElse(Instant.now),
      Instant.now
    ))

    Traverse[List].sequence(subs.map(Submission.store).toList).transact(xa).map(_ => Result.ok)
  }


  def submitNowPlaying[F[_]: Effect](xa: Transactor[F])(account: String, si: SongInfo): F[Result] = {
    val np = NowPlaying(
      account,
      None,
      si.number,
      si.title,
      si.artist,
      si.album,
      si.length,
      si.timeSeconds.map(s => Instant.ofEpochSecond(s.toLong)).getOrElse(Instant.now)
    )
    NowPlaying.update(np).transact(xa).map(_ => Result.ok)
  }

  def getNowPlaying[F[_]: Effect](xa: Transactor[F])(account: String): F[Option[NowPlaying]] =
    NowPlaying.find(account).transact(xa).
      map(opt => opt.filter(_.isValid))

  def recentTracks[F[_]: Effect](xa: Transactor[F], account: String, limit: Int): F[Vector[Submission]] =
    Submission.find(account).
        transact(xa).
        take(limit.toLong).
        compile.
        toVector
}
