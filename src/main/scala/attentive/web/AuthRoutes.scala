package attentive.web

import cats.effect._
import cats.implicits._
import cats.data._
import org.http4s._
import org.http4s.server._
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe.CirceEntityDecoder._
import scala.concurrent.ExecutionContext

import attentive.config._
import attentive.app._
import attentive.app.ops._
import attentive.web.model._

object AuthRoutes {

  def login[F[_]: Effect](
      S: ScrobbleApp[F],
      blockingEc: ExecutionContext,
      cfg: Config
  ): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._

    def makeResponse(res: Login.Result, account: String) =
      res match {
        case Login.Result.Ok(session) =>
          for {
            cd <-
              AuthToken.user(session.name, cfg.serverSecretValue).map(CookieData.apply)
            resp <- Ok(
              LoginResult(session.name, true, "Login successful", Some(cd.asString))
            ).map(_.addCookie(cd.asCookie(cfg)))
          } yield resp
        case _ =>
          Ok(LoginResult(account, false, "Login failed.", None))
      }

    HttpRoutes.of[F] {
      case req @ POST -> Root / "login" =>
        for {
          up <- req.as[UserPass]
          res <- S.loginUserPass(up.account, up.password)
          resp <- makeResponse(res, up.account)
        } yield resp

      case req @ POST -> Root / "session" =>
        authRequest(S.loginSession)(req).flatMap(res => makeResponse(res, ""))

      case GET -> Root / "logout" =>
        Ok().map(
          _.addCookie(ResponseCookie(CookieData.cookieName, "", maxAge = Some(-1)))
        )
    }
  }

  def authRequest[F[_]: Effect](
      auth: String => F[Login.Result]
  )(req: Request[F]): F[Login.Result] =
    CookieData.authenticator(req) match {
      case Right(str) => auth(str)
      case Left(err)  => Login.Result.invalidAuth.pure[F]
    }

  def of[F[_]: Effect](
      S: ScrobbleApp[F]
  )(pf: PartialFunction[AuthedRequest[F, AuthToken], F[Response[F]]]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._

    val authUser = getUser[F](S.loginSession)

    val onFailure: AuthedRoutes[String, F] =
      Kleisli(req => OptionT.liftF(Forbidden(req.authInfo)))

    val middleware: AuthMiddleware[F, AuthToken] =
      AuthMiddleware(authUser, onFailure)

    middleware(AuthedRoutes.of(pf))
  }

  private def getUser[F[_]: Effect](
      auth: String => F[Login.Result]
  ): Kleisli[F, Request[F], Either[String, AuthToken]] =
    Kleisli(r => authRequest(auth)(r).map(_.toEither))
}
