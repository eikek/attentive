package attentive.app.store

import cats.effect._
import org.flywaydb.core.Flyway
import javax.sql.DataSource
import org.log4s._

object Migration {

  private[this] val logger = getLogger

  def migrate[F[_]: Sync](url: String, ds: DataSource): F[Int] =
    Sync[F].delay {
      logger.info("Running db migrations...")
      val locations = dbtype(url) match {
        case Some(dbtype) =>
          List("classpath:db/migration", s"classpath:db/${dbtype.toLowerCase}")
        case None =>
          logger.warn(s"Cannot parse jdbc url: $url. Go with H2")
          List("classpath:db/migration", "classpath:db/h2")
      }

      logger.info(s"Using migration locations: $locations")
      val fw = Flyway.configure().dataSource(ds).locations(locations: _*).load()
      fw.repair()
      fw.migrate()
    }

  private def dbtype(jdbcUrl: String): Option[String] =
    if (jdbcUrl.startsWith("jdbc:")) {
      jdbcUrl.indexOf(':', 5) match {
        case -1 => None
        case n => Some(jdbcUrl.substring(5, n))
      }
    } else {
      None
    }
}
