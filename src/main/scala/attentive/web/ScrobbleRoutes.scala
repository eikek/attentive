package attentive.web

import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import scala.concurrent.ExecutionContext
import java.time._
import org.log4s._

import attentive.config._
import attentive.app._
import attentive.app.ops._

object ScrobbleRoutes {
  private[this] val logger = getLogger

  def routes[F[_]: Effect](S: ScrobbleApp[F], blockingEc: ExecutionContext, cfg: Config): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._

    object hsq extends QueryParamDecoderMatcher[Boolean]("hs")
    object pq extends QueryParamDecoderMatcher[Double]("p")
    object cq extends QueryParamDecoderMatcher[String]("c")
    object vq extends QueryParamDecoderMatcher[String]("v")
    object uq extends QueryParamDecoderMatcher[String]("u")
    object tq extends QueryParamDecoderMatcher[Long]("t")
    object aq extends QueryParamDecoderMatcher[String]("a")


    HttpRoutes.of[F] {
      case req @ POST -> Root /"nowplaying"/"v1" =>
        for {
          form  <- req.as[UrlForm]
          info  = SongInfo.fromMap(form.values)
          _     <- Sync[F].delay(logger.debug(s"Got now playing: $info"))
          auth  <- S.loginSession(info.map(_.token).getOrElse(""))
          resp  <- (info, auth) match {
            case (Some(si), Login.Result.Ok(session)) =>
              S.userApp(session.name).updateNowPlaying(si).flatMap(_ => Ok("OK\n"))
            case (None, _) =>
              BadRequest("FAILED\n")
            case _ =>
              Ok("BADSESSION\n")
          }
        } yield resp

      case req @ POST -> Root / "submissions"/"v1" =>
        for {
          form  <- req.as[UrlForm]
          songs = SubmissionInfo.fromMultiMap(form.values)
          _     <- Sync[F].delay(logger.debug(s"Got submission: $songs"))
          auth  <- S.loginSession(songs.headOption.map(_.token).getOrElse(""))
          resp  <- auth match {
            case Login.Result.Ok(session) =>
              S.userApp(session.name).submit(songs).flatMap(_ => Ok("OK\n"))
            case _ =>
              Ok("BADSESSION\n")
          }
        } yield resp

      case req @ GET -> Root :? hsq(hs) :? pq(p) :? cq(c) :? vq(v) :? uq(u) :? tq(t) :? aq(a) =>
        val token = Login.Token(u, Instant.ofEpochSecond(t), a, Some(Login.ClientInfo(c, v)))
        val uriNP = cfg.baseUrl/"scrobble"/"nowplaying"/"v1"
        val uriSM = cfg.baseUrl/"scrobble"/"submissions"/"v1"
        for {
          res   <- S.login(token)
          resp  <- res match {
            case Login.Result.Ok(session) =>
              Ok(s"""OK
                   |${session.asString}
                   |${uriNP.asString}
                   |${uriSM.asString}
                   |""".stripMargin)
            case Login.Result.InvalidTime =>
              Ok("BADTIME\n")
            case _ =>
              Ok("BADAUTH\n")
          }
        } yield resp
    }
  }
}
