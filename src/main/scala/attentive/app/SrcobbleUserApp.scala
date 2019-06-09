package attentive.app

import attentive.app.ops._
import attentive.app.store._

trait ScrobbleUserApp[F[_]] {
  def accountName: String

  def updateNowPlaying(np: SongInfo): F[Scrobble.Result]

  def getNowPlaying: F[Option[NowPlaying]]

  def submit(songs: SubmissionInfo): F[Scrobble.Result]

  def recentTracks(limit: Int): F[Vector[Submission]]

  def basicStats: F[BasicStats.Result]
}
