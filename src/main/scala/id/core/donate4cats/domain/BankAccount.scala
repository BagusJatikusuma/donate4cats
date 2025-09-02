package id.core.donate4cats.domain

import java.time.LocalDateTime

final case class BankAccount(
  id: BankAccount.Id,
  memberId: Member.Id,
  accountNumber: BankAccount.Number,
  bank: String,
  createdAt: LocalDateTime
)

object BankAccount:
  import cats.effect.*
  import neotype.*
  
  import java.time.format.DateTimeFormatter

  type Id = Id.Type
  object Id extends Newtype[String]:
    override def validate(input: String): Boolean | String = 
      if input.isBlank() then "Bank account id should not be empty"
      else true

  type Number = Number.Type
  object Number extends Newtype[String]:
    override def validate(input: String): Boolean | String = 
      if input.isBlank() then "Bank account number should not be empty"
      else true

  lazy val datetimeFormatter = DateTimeFormatter.ofPattern("yyMMddHHmmssSSS")

  def generateId[F[_]: Sync]: F[Id] = 
    Sync[F].delay {
      val currtime = LocalDateTime.now()
      val id = s"BAC${currtime.format(datetimeFormatter)}"
      Id.make(id).toOption.get
    }