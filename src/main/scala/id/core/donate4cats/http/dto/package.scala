package id.core.donate4cats.http.dto

import cats.*
import cats.effect.*
import cats.syntax.all.*
import org.http4s.*
import org.http4s.EntityDecoder
import org.http4s.multipart.Multipart

implicit val cs: Charset = Charset.`UTF-8`
implicit def stringDecoder[F[_]: Concurrent]: EntityDecoder[F, String] = EntityDecoder.text[F]

case class FilePart(
  fileName: String,
  contentType: MediaType,
  contentLength: Long,
  content: Array[Byte]
)

def textPart[F[_]: Concurrent](m: Multipart[F], name: String): F[Option[String]] =
  m.parts.find(_.name.contains(name)) match {
    case Some(part) => part.as[String].map(Some(_))
    case None       => Applicative[F].pure(None)
  }

// A helper to extract a file part as bytes
def filePart[F[_]: Concurrent](m: Multipart[F], name: String): F[Option[FilePart]] =
  m.parts.find(_.name.contains(name)) match {
    case Some(part) =>
      val fileName    = part.filename
      val contentType = part.contentType.map(_.mediaType)
      val metaOpt = 
        for
          fn <- fileName
          ct <- contentType
        yield (fn, ct)

      metaOpt match
        case None => 
          None.pure[F]
        case Some((fn, mt)) =>   
          part.body.compile.to(Array).map { arr =>
            Some(FilePart(fn, mt, arr.length, arr))  
          }
          
    case None       =>
      Applicative[F].pure(None)
  }
