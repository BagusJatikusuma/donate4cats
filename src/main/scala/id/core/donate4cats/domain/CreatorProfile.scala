package id.core.donate4cats.domain

import cats.effect.*
import io.circe.*
import neotype.interop.circe.given

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

final case class CreatorProfile(
  id: String,
  memberId: Member.Id,
  username: String,
  displayName: String,
  bio: String,
  createdAt: LocalDateTime
) derives Encoder


object CreatorProfile:

  lazy val datetimeFormatter = DateTimeFormatter.ofPattern("yyMMddHHmmssSSS")

  def genId[F[_]: Sync]: F[String] = Sync[F].delay {
    val currtime = LocalDateTime.now()
    s"CRT${currtime.format(datetimeFormatter)}"
  } 