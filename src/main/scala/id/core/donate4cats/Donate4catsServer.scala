package id.core.donate4cats

import cats.effect.Async
import cats.syntax.all.*
import com.comcast.ip4s.*
import fs2.io.net.Network
import fs2.io.file.Files
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
// import org.http4s.server.middleware.Logger
import org.http4s.server.Router
import org.http4s.server.staticcontent._

import id.core.donate4cats.service.CreatorService
import id.core.donate4cats.service.CreatorStorage
import id.core.donate4cats.service.DonationService
import id.core.donate4cats.service.MemberService
import id.core.donate4cats.service.MemberAuth
import id.core.donate4cats.service.MidtransService
import id.core.donate4cats.service.SessionStore
import id.core.donate4cats.service.BankAccountService

import id.core.donate4cats.http.route.MemberRoute
import id.core.donate4cats.http.route.CreatorRoute
import id.core.donate4cats.http.route.DonateRoute
import id.core.donate4cats.http.route.BankAccountRoute
import id.core.donate4cats.http.middleware.ExceptionMiddleware
import id.core.donate4cats.http.middleware.CookieAuthMiddleware

object Donate4catsServer:

  def run[F[_]: Async: Network](
    config: AppConfig,
    memberService: MemberService[F],
    memberAuth: MemberAuth[F],
    sessionStore: SessionStore[F],
    creatorService: CreatorService[F],
    creatorStorage: CreatorStorage[F],
    midtransService: MidtransService[F],
    donationService: DonationService[F],
    bankAccountService: BankAccountService[F]
  ): F[Nothing] = {
    
    given Files[F] = Files.forAsync

    for {
      client <- EmberClientBuilder.default[F].build

      memberRoute   = MemberRoute(memberService, memberAuth, sessionStore)
      creatorRoute  = CreatorRoute(creatorService, creatorStorage)
      donateRoute   = DonateRoute(midtransService, creatorService, donationService)
      bankAccRoute  = BankAccountRoute(bankAccountService)

      authMiddleware = CookieAuthMiddleware.makeMiddleware[F](sessionStore) 

      privateRoutes = memberRoute.protectedRoutes <+> creatorRoute.privateRoutes <+> bankAccRoute.protectedRoutes
      publicRoutes  = memberRoute.publicRoutes <+> creatorRoute.publicRoutes <+> donateRoute.publicRoutes

      httpApp = Router(
        "/assets"   -> fileService[F](FileService.Config(s"${config.workdir.path}/assets")),
        "/private"  -> authMiddleware(privateRoutes),
        ""          -> publicRoutes
      ).orNotFound

      httpAppMiddleware =
        ExceptionMiddleware.catchMiddleware[F]

      finalHttpApp = httpAppMiddleware(httpApp)

      _ <- 
        EmberServerBuilder.default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build
    } yield ()
  }.useForever
