package id.core.donate4cats

import cats.effect.Async
import com.comcast.ip4s.*
import fs2.io.net.Network
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.middleware.Logger

import id.core.donate4cats.service.MemberService
import id.core.donate4cats.service.MemberAuth
import id.core.donate4cats.service.SessionStore
import id.core.donate4cats.http.route.MemberRoute
import id.core.donate4cats.http.middleware.ExceptionMiddleware

object Donate4catsServer:

  def run[F[_]: Async: Network](
    memberService: MemberService[F],
    memberAuth: MemberAuth[F],
    sessionStore: SessionStore[F]
  ): F[Nothing] = {
    for {
      client <- EmberClientBuilder.default[F].build

      memberRoute = MemberRoute(memberService, memberAuth, sessionStore)

      httpApp = (
        memberRoute.publicRoutes
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
