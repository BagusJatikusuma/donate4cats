package id.core.donate4cats

import cats.effect.Async
import cats.syntax.all.*
import com.comcast.ip4s.*
import fs2.io.net.Network
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.middleware.Logger
import org.http4s.server.Router

import id.core.donate4cats.service.MemberService
import id.core.donate4cats.service.MemberAuth
import id.core.donate4cats.service.SessionStore
import id.core.donate4cats.http.route.MemberRoute
import id.core.donate4cats.http.middleware.ExceptionMiddleware
import id.core.donate4cats.http.middleware.CookieAuthMiddleware
import id.core.donate4cats.service.CreatorService
import id.core.donate4cats.http.route.CreatorRoute

object Donate4catsServer:

  def run[F[_]: Async: Network](
    memberService: MemberService[F],
    memberAuth: MemberAuth[F],
    sessionStore: SessionStore[F],
    creatorService: CreatorService[F]
  ): F[Nothing] = {
    for {
      client <- EmberClientBuilder.default[F].build

      memberRoute   = MemberRoute(memberService, memberAuth, sessionStore)
      creatorRoute  = CreatorRoute(creatorService)

      authMiddleware = CookieAuthMiddleware.makeMiddleware[F](sessionStore) 

      privateRoutes = memberRoute.protectedRoutes <+> creatorRoute.privateRoutes

      httpApp = Router(
        "/private"  -> authMiddleware(privateRoutes),
        "/public"   -> memberRoute.publicRoutes
      ).orNotFound

      httpAppMiddleware = 
        ExceptionMiddleware.catchMiddleware[F] andThen Logger.httpApp(true, true)

      finalHttpApp = httpAppMiddleware(httpApp)

      _ <- 
        EmberServerBuilder.default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build
    } yield ()
  }.useForever
