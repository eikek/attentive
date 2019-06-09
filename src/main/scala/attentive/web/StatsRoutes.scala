package attentive.web

import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityEncoder._
import scala.concurrent.ExecutionContext

import attentive.config._
import attentive.app._
import attentive.web.model._

object StatsRoutes {

  def routes[F[_]: Effect](S: ScrobbleApp[F], blockingEc: ExecutionContext, cfg: Config): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case GET -> Root / "basic" =>
        for {
          stats  <- S.basicStats
          resp <- Ok(BasicStats(stats.accounts, stats.submissions, stats.artists, stats.albums))
        } yield resp
    }
  }
}
