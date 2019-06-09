package attentive.web

import org.http4s._
import org.http4s.util._
//import org.http4s.headers.Authorization

import attentive.config.Config
import attentive.app.AuthToken

case class CookieData(auth: AuthToken) {
  def name: String = auth.name
  def asString = auth.asString

  def asCookie(cfg: Config): ResponseCookie = {
    val domain = cfg.baseUrl.hostAndPort
    val sec = cfg.baseUrl.protocol.exists(_.endsWith("s"))
    ResponseCookie(CookieData.cookieName, asString, domain = Some(domain), path = Some("/api/v1"), httpOnly = true, secure = sec)
  }
}
object CookieData {
  val cookieName = "attentive_auth"
  val headerName = "X-Attentive-Auth-Token"

  def authenticator[F[_]](r: Request[F]): Either[String, String] =
    fromCookie(r) orElse fromHeader(r)

  def fromCookie[F[_]](req: Request[F]): Either[String, String] = {
    for {
      header   <- headers.Cookie.from(req.headers).toRight("Cookie parsing error")
      cookie   <- header.values.toList.find(_.name == cookieName).toRight("Couldn't find the authcookie")
    } yield cookie.content
  }

  def fromHeader[F[_]](req: Request[F]): Either[String, String] = {
    req.headers.get(CaseInsensitiveString(headerName)).map(_.value).toRight("Couldn't find an authenticator")
  }

  private implicit class EitherOps[A,B](e: Either[A,B]) {
    def orElse(e2: Either[A,B]): Either[A,B] =
      e match {
        case r@Right(_) => r
        case Left(_) => e2
      }
  }
}
