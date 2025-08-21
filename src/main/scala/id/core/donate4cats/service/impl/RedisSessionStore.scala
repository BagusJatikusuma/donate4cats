package id.core.donate4cats.service.impl

import cats.effect.*
import cats.syntax.all.*
import dev.profunktor.redis4cats.*
import dev.profunktor.redis4cats.effects.*
import io.circe.syntax.*
import io.circe.parser.*
import neotype.interop.cats.given

import id.core.donate4cats.service.SessionStore

import scala.concurrent.duration.*

final class RedisSessionStore[F[_]: Async](
  redis: RedisCommands[F, String, String]
) extends SessionStore[F] {

  private def key(token: SessionStore.SessionToken) = s"session:${token.show}"

  override def create(session: SessionStore.SessionData): F[SessionStore.SessionToken] =
    for
      uuidStr <- Async[F].delay(java.util.UUID.randomUUID().toString)
      token <- SessionStore.SessionToken.make(uuidStr) match
        case Right(value) => Async[F].pure(value)
        case Left(error) => Async[F].raiseError(new IllegalArgumentException(error))

      ttlSeconds = ((session.expiresAt - System.currentTimeMillis()) / 1000).max(0)

      _ <- redis.setEx(key(token), session.asJson.noSpaces, ttlSeconds.seconds)
    yield token

  override def get(token: SessionStore.SessionToken): F[Option[SessionStore.SessionData]] =
    redis.get(key(token)).flatMap {
      case Some(json) => 
        Async[F].fromEither {
          decode[SessionStore.SessionData](json) match
            case Right(data) => Right(Some(data))
            case Left(circeError) => Left(new RuntimeException(s"Failed to decode session data: ${circeError.getMessage}"))
          
        }
      case None => Async[F].pure(None)
    }

  override def delete(token: SessionStore.SessionToken): F[Unit] =
    redis.del(key(token)).void
  
}
