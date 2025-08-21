package id.core.donate4cats.http.dto

import cats.data.*
import cats.effect.*
import cats.syntax.all.*
import org.http4s.*
import org.http4s.circe.*
import io.circe.*
import io.circe.Decoder.Result
import neotype.interop.circe.given

import id.core.donate4cats.domain.Member
import id.core.donate4cats.util.syntax.monad.*

final case class ResetPasswordReq(
  token: String,
  newPassword: Member.Password
)

object ResetPasswordReq:

  private def decode(c: HCursor): ValidatedNel[String, ResetPasswordReq] = 
    (
      c.get[String]("token").toValidatedNelField("Token"),
      c.get[Member.Password]("newPassword").toValidatedNelField("Password")
    ).mapN(ResetPasswordReq.apply)

  given Decoder[ResetPasswordReq] = new Decoder:
    def apply(c: HCursor): Result[ResetPasswordReq] = 
      decode(c).toEither.leftMap(nel => DecodingFailure(nel.toList.mkString(", "), c.history))

  given [F[_]: Concurrent]: EntityDecoder[F, ResetPasswordReq] = jsonOf
