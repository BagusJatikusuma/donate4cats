package id.core.donate4cats.http.dto

final case class CreateCreatorReq(
  username: String,
  displayName: String,
  bio: String,
  file: FilePart
)

object CreateCreatorReq:

  import cats.*
  import cats.effect.*
  import cats.syntax.all.*
  import org.http4s.*
  import org.http4s.multipart.Multipart
  import id.core.donate4cats.util.syntax.monad.*

  def fromMultipart[F[_]: Async](m: Multipart[F]): F[Either[String, CreateCreatorReq]] = 
    imperative {
      for
        fileOpt <- filePart(m, "photo")
        _       <- when(fileOpt.isEmpty) finishWith Left("Photo should not be empty")

        file = fileOpt.get

        _   <- whenNot(file.contentType.isImage) finishWith Left("File is not image")

        usernameOpt <- textPart(m, "username")
        _           <- when(usernameOpt.isEmpty) finishWith Left("Username should not be empty")

        displayNameOpt <- textPart(m, "displayname")
        _              <- when(displayNameOpt.isEmpty) finishWith Left("Display name should not be empty")

        bioOpt  <- textPart(m, "bio")
        _       <- when(bioOpt.isEmpty) finishWith Left("Bio should not be empty")

      yield Right(CreateCreatorReq(usernameOpt.get, displayNameOpt.get, bioOpt.get, file))
    }
