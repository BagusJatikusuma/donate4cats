package id.core.donate4cats.service

import id.core.donate4cats.domain.CreatorProfile
import id.core.donate4cats.http.dto.FilePart

import java.io.InputStream

trait CreatorStorage[F[_]] {

  def savePhoto(creator: CreatorProfile, file: FilePart): F[Unit]

  def getPhoto(creator: CreatorProfile): F[InputStream]
  
}
