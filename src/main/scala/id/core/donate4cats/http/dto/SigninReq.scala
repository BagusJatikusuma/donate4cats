package id.core.donate4cats.http.dto

import cats.data.*
import cats.effect.*
import cats.syntax.all.*
import org.http4s.*
import org.http4s.circe.*
import io.circe.*
import io.circe.Decoder.Result
import neotype.interop.circe.given

import id.core.donate4cats.domain.*
import id.core.donate4cats.util.syntax.monad.*

final case class SigninReq(email: Email, password: Password)

object SigninReq:

  private def decode(c: HCursor): ValidatedNel[String, SigninReq] = 
    (
      c.get[Email]("email").toValidatedNelField("Email"),
      c.get[Password]("password").toValidatedNelField("Password")
    ).mapN(SigninReq.apply)

  given Decoder[SigninReq] = new Decoder:
    def apply(c: HCursor): Result[SigninReq] =
      decode(c).toEither.leftMap { nel => 
        DecodingFailure(nel.toList.mkString(", "), c.history)
      }

  given [F[_]: Concurrent]: EntityDecoder[F, SigninReq] = jsonOf
  