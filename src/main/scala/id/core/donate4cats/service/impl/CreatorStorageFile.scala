package id.core.donate4cats.service.impl

import cats.effect.*
import cats.syntax.all.*
import fs2.io.file.{ Files, Path }
import fs2.Stream

import id.core.donate4cats.AppConfig

import id.core.donate4cats.domain.CreatorProfile
import id.core.donate4cats.service.CreatorStorage
import id.core.donate4cats.http.dto.FilePart

import java.io.InputStream
import java.nio.file.{Files => JFiles, Paths => JPaths}
import java.io.FileInputStream
import java.io.File

final class CreatorStorageFile[F[_]: Async](
  conf: AppConfig
) extends CreatorStorage[F] {

  private val creatorsDir = s"${conf.workdir.path}/assets/creators"

  given Files[F] = Files.forAsync

  override def savePhoto(creator: CreatorProfile, file: FilePart): F[Unit] =
    val creatorDir = s"$creatorsDir/${creator.id}"
    val targetPath = s"$creatorDir/photo.jpg"
    for
      //create dir first if not exist
      _   <- Async[F].blocking(JFiles.createDirectories(JPaths.get(s"$creatorDir")))
      _   <- Stream.emits(file.content)
                   .through(Files[F].writeAll(Path(targetPath)))
                   .compile
                   .drain
    yield ()


  override def getPhoto(creator: CreatorProfile): F[InputStream] =
    val creatorDir = s"$creatorsDir/${creator.id}"
    val targetPath = s"$creatorDir/photo.jpg" 
    Async[F].blocking(new FileInputStream(new File(targetPath)))
  
}
