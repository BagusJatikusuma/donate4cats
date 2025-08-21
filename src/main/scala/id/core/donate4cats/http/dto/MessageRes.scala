package id.core.donate4cats.http.dto

import cats.effect.*
import org.http4s.*
import org.http4s.circe.*
import io.circe.*
import io.circe.generic.semiauto.*

final case class MessageRes(message: String)

object MessageRes:
  given Encoder[MessageRes] = deriveEncoder

  given [F[_]: Concurrent]: EntityEncoder[F, MessageRes] = jsonEncoderOf
