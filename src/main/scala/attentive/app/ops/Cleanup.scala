package attentive.app.ops

import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import java.time._
import org.log4s._

import attentive.config.Config
import attentive.app.store._

object Cleanup {

  private[this] val logger = getLogger

  def cleanup[F[_]: Effect](xa: Transactor[F], cfg: Config): F[Unit] = {
    def threshold =
      Instant.now.minusMillis(cfg.registration.invitationValid.toMillis).minusMillis(30000)

    for {
      _     <- Sync[F].delay(logger.info(s"Starting cleanup"))
      pt    <- Sync[F].delay(threshold)
      n     <- InvitationKey.deleteOlder(pt).transact(xa)
      _     <- Sync[F].delay(logger.info(s"Deleted $n unused invitation keys"))
    } yield ()
  }

}
