package id.core.donate4cats.domain

import cats.*
import cats.effect.*
import cats.syntax.all.*
import org.http4s.EntityEncoder
import org.http4s.circe.*
import io.circe.generic.semiauto.*
import io.circe.{Decoder, Encoder}
import neotype.Newtype
import neotype.interop.cats.given
import neotype.interop.circe.given

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

final case class Member(
  id: Member.Id,
  name: Member.Name,
  email: Member.Email,
  createdAt: LocalDateTime
)

object Member:
  
  type Id = Id.Type
  object Id extends Newtype[String]:
    override inline def validate(input: String): Boolean | String = 
      if input.isBlank() then "Member id should not be empty"
      else true

    extension (id: Id) 
      def asString: String = id.show

  type Name = Name.Type
  object Name extends Newtype[String]:
    override def validate(input: String): Boolean | String = 
      if input.isBlank() then "Member name should not be empty"
      else true

    extension (name: Name)
      def asString: String = name.show

  type Email = Email.Type
  object Email extends Newtype[String]:
    override def validate(input: String): Boolean | String = 
      if input.isBlank() then "Invalid email"
      else true

  type Password = Password.Type
  object Password extends Newtype[String]:
    override def validate(input: String): Boolean | String = 
      if input.isBlank() then "Password should ne be empty"
      else true

    extension (pass: Password)
      def asString: String = pass.show

  lazy val datetimeFormatter = DateTimeFormatter.ofPattern("yyMMddHHmmssSSS")

  def generateId[F[_]: Sync]: F[Id] = 
    Sync[F].delay {
      val currtime = LocalDateTime.now()
      val id = s"MBR${currtime.format(datetimeFormatter)}"
      Id.make(id).toOption.get
    }

  given Encoder[Member] = deriveEncoder
  given Decoder[Member] = deriveDecoder

  given [F[_]: Concurrent]: EntityEncoder[F, Member] = jsonEncoderOf
