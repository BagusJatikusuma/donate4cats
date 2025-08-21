package id.core.donate4cats.service

import neotype.Newtype
import id.core.donate4cats.domain.Member

trait SessionStore[F[_]] {
  
  def create(session: SessionStore.SessionData): F[SessionStore.SessionToken]
  def get(token: SessionStore.SessionToken): F[Option[SessionStore.SessionData]]
  def delete(token: SessionStore.SessionToken): F[Unit]

}

object SessionStore:
  import io.circe.generic.semiauto.*
  import io.circe.{Decoder, Encoder}
  
  case class SessionData(userId: String, expiresAt: Long, payload: Member)
  object SessionData:
    given Encoder[SessionData] = deriveEncoder
    given Decoder[SessionData] = deriveDecoder

  type SessionToken = SessionToken.Type
  object SessionToken extends Newtype[String]:
    override inline def validate(str: String): Boolean | String =
      if str.nonEmpty then true
      else "Session token cannot be empty"
