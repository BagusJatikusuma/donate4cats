package id.core.donate4cats.service

import id.core.donate4cats.domain.Member
import id.core.donate4cats.domain.CreatorProfile

import CreatorService.*

trait CreatorService[F[_]] {

  def getBydId(id: String): F[Option[CreatorProfile]]

  def getByUsername(username: String): F[Option[CreatorProfile]]
  
  def create(member: Member, username: String, displayName: String, bio: String): F[CreateError | CreatorProfile]

  def edit(creator: CreatorProfile, displayName: String, bio: String): F[Unit]

  def delete(creator: CreatorProfile): F[Unit]

}

object CreatorService:

  enum CreateError:
    case UsernameAlreadyTaken
