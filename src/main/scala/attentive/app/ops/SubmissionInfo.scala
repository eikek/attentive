package attentive.app.ops

import fs2._
import cats.data._

final case class SubmissionInfo(songs: Seq[SongInfo]) {

  def isEmpty = songs.isEmpty

  def nonEmpty = songs.nonEmpty

  def map[B](f: SongInfo => B): Seq[B] = songs.map(f)

  def headOption = songs.headOption
}

object SubmissionInfo {

  def fromMultiMap(m: Map[String, Chain[String]]): SubmissionInfo =
    SubmissionInfo(Stream.iterate(0)(_ + 1).
      map(n => SongInfo.fromMap(s"[$n]")(m)).
      unNoneTerminate.
      toVector)

}
