package id.core.donate4cats.service.impl

import cats.effect.*
import cats.syntax.all.*
import dev.profunktor.redis4cats.*
import io.circe.syntax.*
import io.circe.parser.*
import neotype.interop.cats.given

import id.core.donate4cats.service.SessionStore
import id.core.donate4cats.util.syntax.monad.*

import scala.concurrent.duration.*

final class RedisSessionStore[F[_]: Async](
  redis: RedisCommands[F, String, String]
) extends SessionStore[F] {

  private def key(token: SessionStore.SessionToken) = s"session:${token.show}"

  override def create(session: SessionStore.SessionData): F[SessionStore.SessionToken] =
    imperative {
      for
        uuidStr <- runBlock(java.util.UUID.randomUUID().toString)
        token   <- SessionStore.SessionToken.make(uuidStr) match
          case Right(value) => continueWith(value)
          case Left(error)  => errorWith(new IllegalArgumentException(error))

        ttlSeconds = ((session.expiresAt - System.currentTimeMillis()) / 1000).max(0)

        _ <- redis.setEx(key(token), session.asJson.noSpaces, ttlSeconds.seconds)
      yield token
    }

  override def get(token: SessionStore.SessionToken): F[Option[SessionStore.SessionData]] =
    imperative {
      for
        optJsonStr  <- redis.get(key(token))
        _           <- when(optJsonStr.isEmpty) finishWith None
        decodeRes   <- runBlock(decode[SessionStore.SessionData](optJsonStr.get))
        _           <- when(decodeRes.isLeft) throwError {
          val msg = decodeRes.left.toOption.get.getMessage()
          new RuntimeException(s"Failed to decode session data: $msg")
        }
      yield Some(decodeRes.toOption.get)
    }

  override def delete(token: SessionStore.SessionToken): F[Unit] =
    redis.del(key(token)).void
  
}
