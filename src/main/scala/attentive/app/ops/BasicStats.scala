package attentive.app.ops

import cats.implicits._
import cats.effect._
import cats.effect.concurrent.Ref
import doobie._
import doobie.implicits._
import java.time._
import org.log4s._

import attentive.config.Config
import attentive.app.store._

object BasicStats {
  private[this] val logger = getLogger

  case class Result(accounts: Int = 0
    , submissions: Long = 0
    , artists: Long = 0
    , albums: Long = 0
    , created: Instant = Instant.now
  )


  def stats[F[_]: Effect](xa: Transactor[F], cache: Ref[F, Result], cfg: Config): F[Result] = {
    val db = for {
      accn  <- Account.count
      subs  <- Submission.countAll(None)
      artn  <- Submission.countArtists(None)
      albn  <- Submission.countAlbums(None)
    } yield Result(accn, subs, artn, albn)

    cache.get.
      flatMap(r =>
        if (r.submissions > 0 && r.created.plusMillis(cfg.stats.cacheTime.toMillis).isAfter(Instant.now)) r.pure[F]
        else msg("Getting stats from DB") >> db.transact(xa).flatMap(nr => cache.set(nr).map(_ => nr)))
  }

  def userStats[F[_]: Effect](xa: Transactor[F])(account: String): F[Result] = {
     val db = for {
      subs  <- Submission.countAll(Some(account))
      artn  <- Submission.countArtists(Some(account))
      albn  <- Submission.countAlbums(Some(account))
     } yield Result(0, subs, artn, albn)
    db.transact(xa)
  }

  private def msg[F[_]: Effect](s: => String): F[Unit] =
    Sync[F].delay(logger.info(s))
}
