package id.core.donate4cats.domain

import java.time.LocalDateTime

final case class Member private (
  id: Member.Id,
  name: Name,
  email: Email,
  createdAt: LocalDateTime,
  status: Member.Status
) {

  def setActive = this.copy(status = Member.Status.Active)

}

object Member:

  import cats.*
  import cats.effect.*
  import cats.syntax.all.*
  import org.http4s.EntityEncoder
  import org.http4s.circe.*
  import io.circe.generic.semiauto.*
  import io.circe.*
  import io.circe.syntax.*
  import neotype.Newtype
  import neotype.interop.cats.given
  import neotype.interop.circe.given

  import java.time.format.DateTimeFormatter

  def apply(id: Member.Id, name: Name, email: Email, createdAt: LocalDateTime): Member = 
    Member(id, name, email, createdAt, Status.Pending)
  
  type Id = Id.Type
  object Id extends Newtype[String]:
    override inline def validate(input: String): Boolean | String = 
      if input.isBlank() then "Member id should not be empty"
      else true

    extension (id: Id) 
      def asString: String = id.show

  lazy val datetimeFormatter = DateTimeFormatter.ofPattern("yyMMddHHmmssSSS")

  def generateId[F[_]: Sync]: F[Id] = 
    Sync[F].delay {
      val currtime = LocalDateTime.now()
      val id = s"MBR${currtime.format(datetimeFormatter)}"
      Id.make(id).toOption.get
    }

  enum Status:
    case Active, Pending

  object Status:
    import doobie.*
    import io.circe.HCursor
    import io.circe.Decoder.Result
    import io.circe.DecodingFailure

    given Encoder[Status] = Encoder.instance {
      case Active  => "active".asJson
      case Pending => "pending".asJson
    }
    
    given Decoder[Status] with
      def apply(c: HCursor): Result[Status] = 
        for
          str <- c.as[String]
          res <- str match
            case "active"   => Right(Status.Active)
            case "pending"  => Right(Status.Pending)
            case _          => Left(DecodingFailure("Not match option", List())) 
        yield res

    given Meta[Status] =
      Meta[String].tiemap {
        case "active"   => Right(Status.Active)
        case "pending"  => Right(Status.Pending)
        case _          => Left("Value is not in options")
      } {
        case Active   => "active"
        case Pending  => "pending"
      }


  given Encoder[Member] = deriveEncoder
  given Decoder[Member] = deriveDecoder

  given [F[_]: Concurrent]: EntityEncoder[F, Member] = jsonEncoderOf
