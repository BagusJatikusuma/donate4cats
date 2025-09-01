package id.core.donate4cats.http.middleware

import org.http4s.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.AuthMiddleware
import cats.data.*
import cats.effect.*
import cats.syntax.all.*
import neotype.interop.cats.given
import id.core.donate4cats.service.SessionStore
import id.core.donate4cats.service.SessionStore.SessionData
import id.core.donate4cats.service.SessionStore.SessionToken

object CookieAuthMiddleware:
  private val CookieName = "session_id"

  def makeMiddleware[F[_]: Async](store: SessionStore[F]): AuthMiddleware[F, (SessionToken, SessionData)] =
    val dsl = new Http4sDsl[F] {}; import dsl.*

    val authUser: Kleisli[F, Request[F], Either[Response[F], (SessionToken, SessionData)]] =
      Kleisli { req =>
        req.cookies.find(_.name == CookieName) match
          case Some(cookie) =>
            val token = SessionToken.make(cookie.content).toOption.get
            store.get(token).map {
              case Some(session) => Right((token, session))
              case None          => Left(Response[F](status = Status.Unauthorized))
            }
          case None =>
            Async[F].pure(Left(Response[F](status = Status.Unauthorized)))
      }

    val onAuthFailure: AuthedRoutes[Response[F], F] =
      Kleisli(_ => OptionT.liftF(Async[F].pure(Response[F](status = Status.Unauthorized))))

    org.http4s.server.AuthMiddleware(authUser, onAuthFailure)

  def makeSessionCookie[F[_]: Sync](sessionToken: SessionToken, ttlSeconds: Long): ResponseCookie =
    ResponseCookie(
      name     = CookieName,
      content  = sessionToken.show,
      httpOnly = true,
      secure   = true,               // for HTTPS only, false for local dev
      sameSite = Some(SameSite.Strict),
      maxAge   = Some(ttlSeconds),
      path     = Some("/")           // ensure it applies to whole site
    )
  
  def removeSessionCookie: ResponseCookie =
    ResponseCookie(
      name     = CookieName,
      content  = "deleted",
      httpOnly = true,
      secure   = true,               // for HTTPS only, false for local dev
      sameSite = Some(SameSite.Strict),
      maxAge   = Some(1),
      path     = Some("/")           // ensure it applies to whole site
    )