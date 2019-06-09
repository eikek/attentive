package attentive.web

import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityEncoder._
import scala.concurrent.ExecutionContext
import java.time._

import attentive.config._
import attentive.app._
import attentive.web.model._

object UserRoutes {
  private val zoneId = ZoneId.of("UTC")

  case class User(name: String)

  def routes[F[_]: Effect](S: ScrobbleApp[F], blockingEc: ExecutionContext, cfg: Config): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._

    object Limit extends OptionalQueryParamDecoderMatcher[Int]("limit")

    AuthRoutes.of[F](S) {
      case GET -> Root / "nowplaying" as user =>
        for {
          np     <- S.userApp(user.name).getNowPlaying
          track  = np.map(n => Track(n.title, n.number, n.artist, n.album, n.length, localDate(n.created), n.created.toEpochMilli))
          resp   <- track.map(Ok(_)).getOrElse(NotFound())
        } yield resp

      case GET -> Root / "recenttracks" :? Limit(limit) as user =>
        for {
          recent   <- S.userApp(user.name).recentTracks(math.min(100, limit.getOrElse(50)))
          tracks = RecentTracks(recent.map(s => Track(s.title, s.number, s.artist, s.album, s.length, localDate(s.submissionTime), s.submissionTime.toEpochMilli)).toList)
          resp     <- Ok(tracks)
        } yield resp

      case GET -> Root / "stats" as user =>
        for {
          stats  <- S.userApp(user.name).basicStats
          resp <- Ok(BasicStats(stats.accounts, stats.submissions, stats.artists, stats.albums))
        } yield resp
    }

  }

  private def localDate(i: Instant) =
    i.atZone(zoneId).toLocalDateTime
}
