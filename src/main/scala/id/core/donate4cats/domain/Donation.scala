package id.core.donate4cats.domain

import java.time.LocalDateTime

final case class Donatur(
  name: String,
  email: String
)

final case class Donation(
  id: String,
  creatorId: String,
  amount: Double,
  message: String,
  donatur: Donatur,
  createdAt: LocalDateTime
)

object Donation:

  import cats.effect.*
  import java.time.format.DateTimeFormatter

  lazy val datetimeFormatter = DateTimeFormatter.ofPattern("yyMMddHHmmssSSS")

  def genId[F[_]: Sync]: F[String] = Sync[F].delay {
    val currtime = LocalDateTime.now()
    s"CRT${currtime.format(datetimeFormatter)}"
  }