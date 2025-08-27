package id.core.donate4cats

import cats.effect.*

import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.effect.Log.Stdout.given
import id.core.donate4cats.service.DoobieDatabase
import id.core.donate4cats.service.impl.MemberAuthServiceLive
import id.core.donate4cats.service.impl.RedisSessionStore
import id.core.donate4cats.service.impl.MemberServiceLive
import id.core.donate4cats.service.impl.CreatorServiceLive
import id.core.donate4cats.service.impl.CreatorStorageFile

object Main extends IOApp.Simple:
  val run =
    val getResource = 
      for
        config    <- AppConfig.getConfig[IO]
        redis     <- Redis[IO].utf8(config.redis.url)
        database  <- DoobieDatabase.makeTransactor[IO](
          config.database.driver,
          config.database.jdbcUrl
        )
      yield (config, redis, database)
    
    getResource.use { case(config, redis, xa) =>
      
      val memberAuth      = MemberAuthServiceLive[IO](xa)
      val sessionStore    = RedisSessionStore[IO](redis)
      val memberService   = MemberServiceLive[IO](xa)
      val creatorStorage  = CreatorStorageFile[IO](config)
      val creatorService  = CreatorServiceLive[IO](xa, creatorStorage)

      Donate4catsServer.run[IO](memberService, memberAuth, sessionStore, creatorService, creatorStorage)
    }
