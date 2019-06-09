package attentive.web

import cats.effect._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.implicits._
import fs2.Stream
import scala.concurrent.ExecutionContext

import org.http4s.server.middleware.Logger
import org.http4s.server.Router
import attentive.config.Config
import attentive.app._

object AttentiveServer {

  def stream[F[_]: ConcurrentEffect](cfg: Config, blockingEc: ExecutionContext)
    (implicit T: Timer[F], C: ContextShift[F]): Stream[F, Nothing] = {
    val app = for {
      scrobbleApp  <- ScrobbleAppImpl.create[F](cfg, blockingEc)
      _            <- Resource.liftF(scrobbleApp.init)

      httpApp = Router(
        "/api/info" -> InfoRoutes.infoRoutes(cfg),
        "/api/v1/account" -> AccountRoutes.routes[F](scrobbleApp, blockingEc, cfg),
        "/api/v1/stats" -> StatsRoutes.routes[F](scrobbleApp, blockingEc, cfg),
        "/api/v1/auth" -> AuthRoutes.login[F](scrobbleApp, blockingEc, cfg),
        "/api/v1/user" -> UserRoutes.routes[F](scrobbleApp, blockingEc, cfg),
        "/scrobble" -> ScrobbleRoutes.routes[F](scrobbleApp, blockingEc, cfg),
        "/app/assets" -> WebjarRoutes.appRoutes[F](blockingEc, cfg),
        "/app" -> TemplateRoutes.indexRoutes[F](blockingEc, cfg)
      ).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(false, false)(httpApp)

    } yield finalHttpApp


    Stream.resource(app).flatMap(httpApp =>
      BlazeServerBuilder[F]
        .bindHttp(cfg.bind.port, cfg.bind.host)
        .withHttpApp(httpApp)
        .serve
    )

  }.drain
}
