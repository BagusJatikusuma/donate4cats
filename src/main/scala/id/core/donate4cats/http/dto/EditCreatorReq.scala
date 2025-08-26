package id.core.donate4cats.http.dto

import io.circe.*

final case class EditCreatorReq(
  creatorId: String,
  displayName: String,
  bio: String
) derives Decoder

object EditCreatorReq:
  import cats.effect.*
  import org.http4s.EntityDecoder
  import org.http4s.circe.*

  given [F[_]: Concurrent]: EntityDecoder[F, EditCreatorReq] = jsonOf