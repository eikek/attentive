package attentive.app.ops

import cats.data._

case class SongInfo(
  token: String
    , number: Int
    , title: String
    , artist: String
    , album: String
    , length: Int
    , mbid: String
    , timeSeconds: Option[Int]
)

object SongInfo {

  def fromMap(m: Map[String, Chain[String]]): Option[SongInfo] =
    fromMap("")(m)

  def fromMap(suffix: String)(m: Map[String, Chain[String]]): Option[SongInfo] = {
    val data = m.withDefault(_ => Chain.empty)

    for {
      s <- data(s"s").headOption
      t <- data(s"t$suffix").headOption
      b <- data(s"b$suffix").headOption
    } yield SongInfo(
      token = s
        , number = data(s"n$suffix").headOption.map(_.toInt).getOrElse(0)
        , title = t
        , artist = data(s"a$suffix").headOption.getOrElse("")
        , album = b
        , length = data(s"l$suffix").headOption.map(_.toInt).getOrElse(0)
        , mbid = data(s"m$suffix").headOption.getOrElse("")
        , timeSeconds = data(s"i$suffix").headOption.map(_.toInt)
    )
  }
}
