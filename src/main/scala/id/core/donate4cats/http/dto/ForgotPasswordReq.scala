package id.core.donate4cats.http.dto

import cats.effect.*
import org.http4s.*
import org.http4s.circe.*
import io.circe.*
import io.circe.generic.semiauto.*
import neotype.interop.circe.given

import id.core.donate4cats.domain.Member

final case class ForgotPasswordReq(email: Member.Email)

object ForgotPasswordReq:

  given Decoder[ForgotPasswordReq] = deriveDecoder
  given [F[_]: Concurrent]: EntityDecoder[F, ForgotPasswordReq] = jsonOf
