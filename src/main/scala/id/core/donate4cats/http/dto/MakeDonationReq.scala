package id.core.donate4cats.http.dto

import io.circe.*

final case class MakeDonationReq(
  name: String,
  email: String,
  amount: Double,
  creatorId: String
) derives Decoder

object MakeDonationReq:
  import cats.effect.*
  import org.http4s.EntityDecoder
  import org.http4s.circe.*

  given [F[_]: Concurrent]: EntityDecoder[F, MakeDonationReq] = jsonOf