package attentive.app.ops

import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._

import attentive.config.Config
import attentive.app.store._


object NewInvite {

  sealed trait Result
  object Result {
    case class Ok(key: InvitationKey) extends Result
    case object InvalidPassword extends Result
    case object Disabled extends Result

    def ok(key: InvitationKey): Result = Ok(key)
    def invalidPassword: Result = InvalidPassword
    def disabled: Result = Disabled
  }

  def generate[F[_]: Effect](xa: Transactor[F], cfg: Config)(pass: String): F[Result] = {
    cfg.registration.mode match {
      case Config.Registration.Mode.Closed =>
        Result.disabled.pure[F]
      case Config.Registration.Mode.Open =>
        Result.disabled.pure[F]
      case Config.Registration.Mode.Invite =>
        if (pass != cfg.registration.invitationKey ||
            cfg.registration.invitationKey.isEmpty) Result.invalidPassword.pure[F]
        else InvitationKey.generate[F].
          flatMap(k => InvitationKey.store(k).transact(xa).map(_ => k)).
          map(k => Result.ok(k))
    }
  }
}
