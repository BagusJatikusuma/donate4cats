package id.core.donate4cats

import cats.effect.*
import pureconfig.*
import pureconfig.module.catseffect.syntax.*

final case class DatabaseConfig(driver: String, jdbcUrl: String) derives ConfigReader

final case class RedisConfig(url: String) derives ConfigReader

final case class WorkDir(path: String) derives ConfigReader

final case class AppConfig(database: DatabaseConfig, redis: RedisConfig, workdir: WorkDir) derives ConfigReader

object AppConfig:

  def getConfig[F[_]: Sync]: Resource[F, AppConfig] = 
    Resource.eval(ConfigSource.default.loadF[F, AppConfig]())