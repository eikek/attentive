package attentive.app.ops

import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import scodec.bits.ByteVector
import java.time._

import attentive.config.Config
import attentive.app.store._

object Register {

  case class Data(name: String, passwordPlain: String, invite: Option[String])

  sealed trait Result
  object Result {

    case object AccountExists extends Result
    case object Disabled extends Result
    case object InvalidKey extends Result
    case object Ok extends Result

    def accountExists: Result = AccountExists
    def disabled: Result = Disabled
    def invalidKey: Result = InvalidKey
    def ok: Result = Ok

  }

  def register[F[_]: Effect](xa: Transactor[F], cfg: Config)(data: Data): F[Result] = {
    val account = Account(data.name, true, md5(data.passwordPlain), Instant.now)
    val db: ConnectionIO[Result] = Account.exists(data.name).flatMap {
      case true =>
        Result.accountExists.pure[ConnectionIO]
      case false =>
        Account.store(account).map(_ => Result.ok)
    }

    cfg.registration.mode match {
      case Config.Registration.Mode.Closed =>
        Result.disabled.pure[F]
      case Config.Registration.Mode.Open =>
        db.transact(xa)
      case Config.Registration.Mode.Invite =>
        data.invite match {
          case Some(key) =>
            InvitationKey.use(key, cfg.registration.invitationValid).
              flatMap({
                case true => db
                case false => Result.invalidKey.pure[ConnectionIO]
              }).
              transact(xa)
          case None =>
            Result.disabled.pure[F]
        }
    }
  }

  def accountExists[F[_]: Effect](xa: Transactor[F])(name: String): F[Boolean] =
    Account.exists(name).transact(xa)

  private def md5(s: String): String =
    ByteVector.view(s.getBytes).digest("MD5").toHex
}
