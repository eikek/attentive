package attentive

import cats.effect._
import cats.implicits._
import java.util.concurrent.atomic.AtomicReference
import java.security.SecureRandom
import scodec.bits.ByteVector

object Util {

  def memo[F[_]: Sync, A](fa: => F[A]): F[A] = {
    val ref = new AtomicReference[A]()
    Sync[F].suspend {
      Option(ref.get) match {
        case Some(a) => a.pure[F]
        case None =>
          fa.map(a => {
            ref.set(a)
            a
          })
      }
    }
  }

  def genSalt[F[_]: Sync]: F[Array[Byte]] =
    Sync[F].delay {
      val sr = new SecureRandom
      val buffer = new Array[Byte](16)
      sr.nextBytes(buffer)
      buffer
    }

  def newSalt[F[_]: Sync]: F[String] =
    genSalt[F].map(a => ByteVector.view(a).toBase64)
}
