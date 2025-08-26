package id.core.donate4cats

import cats.effect.*

import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.effect.Log.Stdout.given
import id.core.donate4cats.service.DoobieDatabase
import id.core.donate4cats.service.impl.MemberAuthServiceLive
import id.core.donate4cats.service.impl.RedisSessionStore
import id.core.donate4cats.service.impl.MemberServiceLive
import id.core.donate4cats.service.impl.CreatorServiceLive

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
      yield (redis, database)
    
    getResource.use { case(redis, xa) =>
      
      val memberAuth      = MemberAuthServiceLive(xa)
      val sessionStore    = RedisSessionStore(redis)
      val memberService   = MemberServiceLive(xa)
      val creatorService  = CreatorServiceLive(xa)

      Donate4catsServer.run[IO](memberService, memberAuth, sessionStore, creatorService)
    }
