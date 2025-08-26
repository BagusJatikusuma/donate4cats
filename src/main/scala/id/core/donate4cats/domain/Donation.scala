package id.core.donate4cats.domain

import java.time.LocalDateTime

final case class Donatur(
  name: String,
  email: String
)

final case class Donation(
  id: String,
  memberId: String,
  amount: Double,
  message: String,
  donatur: Donatur,
  createdAt: LocalDateTime
)
