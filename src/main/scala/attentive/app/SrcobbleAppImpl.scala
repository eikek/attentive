package attentive.app

import fs2.Stream
import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import doobie._
import doobie.hikari._
import scala.concurrent.ExecutionContext
import org.slf4j._
import com.zaxxer.hikari.util.DriverDataSource
import java.util.Properties
import scala.concurrent.duration._

import attentive.config._
import attentive.app.store._
import attentive.app.ops._

final class ScrobbleAppImpl[F[_]: ConcurrentEffect: Timer](xa: Transactor[F], cfg: Config, blockingEc: ExecutionContext, statsCache: Ref[F, BasicStats.Result])
    extends ScrobbleApp[F] {

  private[this] val logger = LoggerFactory.getLogger(getClass)

  logger.info("init app")

  def init: F[Unit] = {
    val task = Stream.fixedDelay(2.hours).
      evalMap(_ => Cleanup.cleanup(xa, cfg)).
      compile.drain

    Concurrent[F].start(task).map(_ => ())
  }

  def register(reg: Register.Data): F[Register.Result] =
    Register.register[F](xa, cfg)(reg)

  def newInvite(pass: String): F[NewInvite.Result] =
    NewInvite.generate(xa, cfg)(pass)

  def accountExists(name: String): F[Boolean] =
    Register.accountExists(xa)(name)

  def login(token: Login.Token): F[Login.Result] =
    Login.loginToken(xa, cfg)(token)

  def loginUserPass(user: String, pass: String): F[Login.Result] =
    Login.loginUserPass(xa, cfg)(Login.UserPass(user, pass, None))

  def loginSession(key: String): F[Login.Result] =
    Login.loginSession(xa, cfg)(key)

  def basicStats: F[BasicStats.Result] =
    BasicStats.stats(xa, statsCache, cfg)

  def userApp(account: String): ScrobbleUserApp[F] = new ScrobbleUserApp[F] {
    val accountName = account

    def updateNowPlaying(np: SongInfo): F[Scrobble.Result] =
      Scrobble.submitNowPlaying(xa)(account, np)

    def getNowPlaying: F[Option[NowPlaying]] =
      Scrobble.getNowPlaying(xa)(account)

    def submit(songs: SubmissionInfo): F[Scrobble.Result] =
      Scrobble.submitTrack(xa)(account, songs)

    def recentTracks(limit: Int): F[Vector[Submission]] =
      Scrobble.recentTracks(xa, account, limit)

    def basicStats: F[BasicStats.Result] =
      BasicStats.userStats(xa)(account)
  }
}

object ScrobbleAppImpl {

  def create[F[_]: ConcurrentEffect](cfg: Config
    , blockingEc: ExecutionContext)
    (implicit CS: ContextShift[F], T: Timer[F])
      : Resource[F, ScrobbleApp[F]] =
    for {
      ce   <- ExecutionContexts.fixedThreadPool[F](cfg.jdbc.poolsize)
      te   <- ExecutionContexts.cachedThreadPool[F]
      xa   <- HikariTransactor.newHikariTransactor[F](
        cfg.jdbc.driver,
        cfg.jdbc.url,
        cfg.jdbc.user,
        cfg.jdbc.password,
        ce, te
      )
      ds   <- simpleDs(cfg.jdbc)
      _    <- Resource.liftF(Migration.migrate(cfg.jdbc.url, ds))
      statsCache <- Resource.liftF(Ref.of(BasicStats.Result()))
    } yield new ScrobbleAppImpl(xa, cfg, blockingEc, statsCache)


  private def simpleDs[F[_]: Effect](jdbc: Config.Jdbc): Resource[F, DriverDataSource] =
    Resource.make(Effect[F].delay(new DriverDataSource(jdbc.url, jdbc.driver, new Properties, jdbc.user, jdbc.password)))(_ => ().pure[F])

}
