package attentive.web

import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe.CirceEntityDecoder._
import scala.concurrent.ExecutionContext

import attentive.config._
import attentive.app._
import attentive.app.ops._
import attentive.web.model._

object AccountRoutes {

  def routes[F[_]: Effect](S: ScrobbleApp[F], blockingEc: ExecutionContext, cfg: Config): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case req @ POST -> Root / "newinvite" =>
        for {
          invp <- req.as[InvitePassword]
          res  <- S.newInvite(invp.password)
          resp <- res match {
            case NewInvite.Result.Ok(ik) =>
              Ok(Invitation(ik.key, cfg.registration.invitationValid.toMillis))
            case NewInvite.Result.InvalidPassword =>
              BadRequest(Failure(1, "Invalid invitation password"))
            case NewInvite.Result.Disabled =>
              UnprocessableEntity(Failure(2, "Registration disabled."))
          }
        } yield resp

      case req @ POST -> Root / "register" =>
        for {
          reg  <- req.as[Registration]
          res  <- S.register(Register.Data(reg.name, reg.password, reg.invitation))
          resp <- res match {
            case Register.Result.Ok =>
              Ok()
            case Register.Result.AccountExists =>
              UnprocessableEntity(Failure(3, "Account exists"))
            case Register.Result.Disabled =>
              UnprocessableEntity(Failure(4, "Registration disabled"))
            case Register.Result.InvalidKey =>
              BadRequest(Failure(5, "Invalid invitation key"))
          }
        } yield resp

      case GET -> Root / "exists" / name =>
        for {
          res  <- S.accountExists(name)
          resp <- Ok(AccountExists(name, res))
        } yield resp
    }
  }
}
