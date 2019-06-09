package attentive.config

import java.nio.file.{Path, Paths}
import pureconfig._
import pureconfig.generic.ProductHint
import pureconfig.generic.auto._
import pureconfig.ConvertHelpers._
import pureconfig.error.CannotConvert
import scala.concurrent.duration.Duration
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import attentive.data.Uri

case class Config(appName: String
  , baseUrl: Uri
  , serverSecret: String
  , registration: Config.Registration
  , auth: Config.Auth
  , bind: Config.Bind
  , jdbc: Config.Jdbc
  , stats: Config.Stats) {

  def serverSecretValue: Array[Byte] = {
    if (serverSecret.nonEmpty) serverSecret.getBytes(Config.utf8)
    else Option(Config.serverSecret.get) match {
      case Some(s) => s.getBytes(Config.utf8)
      case None =>
        val s = UUID.randomUUID.toString
        Config.serverSecret.compareAndSet(null, s)
        s.getBytes(Config.utf8)
    }
  }
}

object Config {
  private val serverSecret = new AtomicReference[String](null)
  private val utf8 = java.nio.charset.StandardCharsets.UTF_8

  implicit def hint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, KebabCase))

  implicit val pathConvert: ConfigReader[Path] = ConfigReader.fromString[Path](catchReadError(s =>
    if (s.isEmpty) throw new Exception("Empty path is not allowed: "+ s)
    else Paths.get(s).toAbsolutePath.normalize
  ))

  implicit val uriConvert: ConfigReader[Uri] = ConfigReader.fromString[Uri](s =>
    Uri.parse(s).left.map(err => CannotConvert(s, "Uri", err))
  )

  implicit val registrationModeConvert: ConfigReader[Registration.Mode] = ConfigReader.fromString[Registration.Mode](catchReadError(s =>
    s.toLowerCase match {
      case "open" => Registration.Mode.Open
      case "closed" => Registration.Mode.Closed
      case _ => Registration.Mode.Invite
    }
  ))

  case class Bind(host: String, port: Int)

  case class Jdbc(url: String, user: String, password: String, driver: String, poolsize: Int)

  case class Registration(mode: Registration.Mode, invitationKey: String, invitationValid: Duration)

  object Registration {
    sealed trait Mode
    object Mode {
      case object Open extends Mode
      case object Closed extends Mode
      case object Invite extends Mode
    }
  }

  case class Auth(tokenValid: Duration
    , sessionValid: Duration
  )

  case class Stats(cacheTime: Duration)

  lazy val default: Config = {
    loadConfigOrThrow[Config]("attentive")
  }
}
