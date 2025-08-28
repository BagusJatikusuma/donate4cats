package id.core.donate4cats.vendor.midtrans.snap

import cats.effect.*
import io.circe.*
import org.http4s.*
import org.http4s.circe.*

final case class SnapCredential(token: String, redirectUrl: String) derives Encoder

object SnapCredential:
  given Decoder[SnapCredential] with
    final def apply(c: HCursor): Decoder.Result[SnapCredential] =
      for
        token       <- c.downField("token").as[String]
        redirectUrl <- c.downField("redirect_url").as[String]
      yield SnapCredential(token, redirectUrl) 

  given [F[_]: Concurrent]: EntityDecoder[F, SnapCredential] = jsonOf
