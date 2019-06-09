package attentive.app.ops

import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import scodec.bits.ByteVector
import java.time._
import org.log4s._

import attentive.config.Config
import attentive.app.store._
import attentive.app.AuthToken

object Login {
  private[this] val logger = getLogger

  case class ClientInfo(name: String, version: String)

  case class Token(user: String, time: Instant, token: String, client: Option[ClientInfo])

  case class UserPass(user: String, pass: String, client: Option[ClientInfo]) {
    def hidePass: UserPass =
      if (pass.isEmpty) copy(pass = "<none>")
      else copy(pass = "***")
  }

  sealed trait Result {
    def toEither: Either[String, AuthToken]
  }
  object Result {
    case class Ok(session: AuthToken) extends Result {
      val toEither = Right(session)
    }
    case object InvalidAuth extends Result {
      val toEither = Left("Authentication failed.")
    }
    case object InvalidTime extends Result {
      val toEither = Left("Authentication failed.")
    }

    def ok(session: AuthToken): Result = Ok(session)
    def invalidAuth: Result = InvalidAuth
    def invalidTime: Result = InvalidTime
  }

  def loginToken[F[_]: Effect](xa: Transactor[F], cfg: Config)(token: Token): F[Result] = {
    logResult(token) {
      if (authExpired(cfg, token.time)) Result.invalidTime.pure[F]
      else Account.findActive(token.user).
        transact(xa).
        flatMap({
          case Some(acc) =>
            val t = md5(acc.password + (token.time.toEpochMilli / 1000))
            if (t != token.token) Result.invalidAuth.pure[F]
            else {
              AuthToken.user[F](acc.name, cfg.serverSecretValue).map(Result.ok)
            }
          case None =>
            Result.invalidAuth.pure[F]
        })
    }
  }

  def loginUserPass[F[_]: Effect](xa: Transactor[F], cfg: Config)(up: UserPass): F[Result] =
    logResult(up.hidePass) {
      Account.findActive(up.user).
        transact(xa).
        flatMap({
          case Some(acc) =>
            val t = md5(up.pass)
            if (t != acc.password) Result.invalidAuth.pure[F]
            else {
              AuthToken.user[F](acc.name, cfg.serverSecretValue).map(Result.ok)
            }
          case None =>
            Result.invalidAuth.pure[F]
        })
    }

  def loginSession[F[_]: Effect](xa: Transactor[F], cfg: Config)(sessionKey: String): F[Result] =
    AuthToken.fromString(sessionKey) match {
      case Right(at) =>
        if (at.sigInvalid(cfg.serverSecretValue)) Result.invalidAuth.pure[F]
        else if (at.isExpired(cfg.auth.sessionValid)) Result.invalidTime.pure[F]
        else Result.ok(at).pure[F]
      case Left(err) =>
        Result.invalidAuth.pure[F]
    }

  private def logResult[F[_]: Effect](in: Any)(res: F[Result]): F[Result] =
    res.map({ r =>
      logger.info(s"Authenticating $in => $r")
      r
    })

  private def authExpired(cfg: Config, time: Instant): Boolean =
    time.plusMillis(cfg.auth.tokenValid.toMillis).isBefore(Instant.now)

  private def md5(s: String): String =
    ByteVector.view(s.getBytes).digest("MD5").toHex.toLowerCase

}
