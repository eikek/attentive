package attentive.app

import cats.effect._
import cats.implicits._
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import scodec.bits.ByteVector
import scala.concurrent.duration._

import attentive.Util
import AuthToken._

case class AuthToken(millis: Long, name: String, salt: String, sig: String) {
  def asString = s"$millis-${b64enc(name)}-${salt}-${sig}"

  def sigValid(key: Array[Byte]): Boolean = {
    val newSig = AuthToken.sign(this, key)
    AuthToken.constTimeEq(sig, newSig)
  }
  def sigInvalid(key: Array[Byte]): Boolean =
    !sigValid(key)

  def notExpired(validity: Duration): Boolean =
    !isExpired(validity)

  def isExpired(validity: Duration): Boolean = {
    val ends = Instant.ofEpochMilli(millis).plusMillis(validity.toMillis)
    Instant.now.isAfter(ends)
  }

  def validate(key: Array[Byte], validity: Duration): Boolean =
    sigValid(key) && notExpired(validity)
}

object AuthToken {
  private val utf8 = java.nio.charset.StandardCharsets.UTF_8

  def fromString(s: String): Either[String, AuthToken] =
    s.split("\\-", 4) match {
      case Array(ms, ns, salt, sig) =>
        for {
          millis <- asInt(ms).toRight("Cannot read authenticator data")
          name   <- b64dec(ns).toRight("Cannot read authenticator data")
        } yield AuthToken(millis, name, salt, sig)

      case _ =>
        Left("Invalid authenticator")
    }

  def user[F[_]: Sync](name: String, key: Array[Byte]): F[AuthToken] = {
    for {
      salt   <- Util.newSalt[F]
      millis = Instant.now.toEpochMilli
      cd = AuthToken(millis, name, salt, "")
      sig = sign(cd, key)
    } yield cd.copy(sig = sig)
  }

  private def sign(cd: AuthToken, key: Array[Byte]): String = {
    val raw = cd.millis + cd.name + cd.salt
    val mac = Mac.getInstance("HmacSHA1")
    mac.init(new SecretKeySpec(key, "HmacSHA1"))
    ByteVector.view(mac.doFinal(raw.getBytes(utf8))).toBase64
  }

  private def b64enc(s: String): String =
    ByteVector.view(s.getBytes(utf8)).toBase64

  private def b64dec(s: String): Option[String] =
    ByteVector.fromValidBase64(s).decodeUtf8.toOption

  private def asInt(s: String): Option[Long] =
    try {
      Some(s.toLong)
    } catch {
      case _: Exception => None
    }

  private def constTimeEq(s1: String, s2: String): Boolean =
    s1.zip(s2).foldLeft(true)({ case (r, (c1, c2)) => r & c1 == c2 }) & s1.length == s2.length

}
