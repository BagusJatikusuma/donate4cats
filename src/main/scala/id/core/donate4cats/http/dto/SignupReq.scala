package id.core.donate4cats.http.dto

import cats.data.*
import cats.effect.*
import cats.syntax.all.*
import org.http4s.*
import org.http4s.circe.*
import io.circe.*
import io.circe.generic.semiauto.*
import io.circe.Decoder.Result
import neotype.interop.circe.given

import id.core.donate4cats.domain.*
import id.core.donate4cats.util.syntax.monad.*

final case class SignupReq(name: Name, email: Email, password: Password)

object SignupReq:

  private def decode(c: HCursor): ValidatedNel[String, SignupReq] = 
    (
      c.get[Name]("name").toValidatedNelField("Name"),
      c.get[Email]("email").toValidatedNelField("Email"),
      c.get[Password]("password").toValidatedNelField("Password")
    ).mapN(SignupReq.apply)

  //in case you would like to accumulate error use this one
  val applicativeDecoder: Decoder[SignupReq] = new Decoder:
    def apply(c: HCursor): Result[SignupReq] =
      decode(c).toEither.leftMap { nel => 
        DecodingFailure(nel.toList.mkString(", "), c.history)
      }

  given Decoder[SignupReq] = deriveDecoder 

  given [F[_]: Concurrent]: EntityDecoder[F, SignupReq] = jsonOf
