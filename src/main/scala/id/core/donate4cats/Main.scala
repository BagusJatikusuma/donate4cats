package id.core.donate4cats

import cats.effect.*
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.effect.Log.Stdout.given
import org.http4s.ember.client.EmberClientBuilder

import id.core.donate4cats.service.DoobieDatabase
import id.core.donate4cats.service.MidtransService

import id.core.donate4cats.service.impl.MemberAuthServiceLive
import id.core.donate4cats.service.impl.RedisSessionStore
import id.core.donate4cats.service.impl.MemberServiceLive
import id.core.donate4cats.service.impl.CreatorServiceLive
import id.core.donate4cats.service.impl.CreatorStorageFile
import id.core.donate4cats.service.impl.DonationServiceLive
import id.core.donate4cats.service.impl.BankAccountServiceLive

import id.core.donate4cats.vendor.midtrans.snap.MidtransConfig
import id.core.donate4cats.vendor.midtrans.snap.MidtransSnap

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
        httpClient <- EmberClientBuilder.default[IO].build

      yield (config, redis, database, httpClient)
    
    getResource.use { case(config, redis, xa, httpClient) =>
      
      val midConfig = MidtransConfig(config.midtrans.prod, config.midtrans.merchantId, config.midtrans.clientKey, config.midtrans.serverKey)
      val midSnap   = MidtransSnap[IO](midConfig, httpClient)

      val midtransService = MidtransService(midSnap, xa)

      val memberAuth      = MemberAuthServiceLive[IO](xa)
      val sessionStore    = RedisSessionStore[IO](redis)
      val memberService   = MemberServiceLive[IO](xa)
      val creatorStorage  = CreatorStorageFile[IO](config)
      val creatorService  = CreatorServiceLive[IO](xa, creatorStorage)
      val donationService = DonationServiceLive[IO](xa)
      val bankAccService  = BankAccountServiceLive[IO](xa)

      Donate4catsServer.run[IO](
        config, 
        memberService, 
        memberAuth, 
        sessionStore, 
        creatorService, 
        creatorStorage, 
        midtransService, 
        donationService,
        bankAccService
      )
    }
