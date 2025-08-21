package id.core.donate4cats.service

import cats.effect.*
import _root_.doobie.hikari.HikariTransactor
import com.zaxxer.hikari.HikariConfig

object DoobieDatabase:

  def makeTransactor[F[_]: Async](
    driver: String,
    jdbcUrl: String,
  ): Resource[F, HikariTransactor[F]] =
    for
      conf <- Resource.pure {
        val config = new HikariConfig()

        config.setDriverClassName(driver)
        config.setJdbcUrl(jdbcUrl)
        
        config
      }
      xa <- HikariTransactor.fromHikariConfig[F](conf)
    yield xa

