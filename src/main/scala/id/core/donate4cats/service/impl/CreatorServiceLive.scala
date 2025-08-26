package id.core.donate4cats.service.impl

import cats.effect.*
import cats.syntax.all.*
import doobie.*
import doobie.implicits.*

import id.core.donate4cats.util.syntax.monad.*

import id.core.donate4cats.service.query.CreatorQuery
import id.core.donate4cats.service.CreatorService
import id.core.donate4cats.service.CreatorService.CreateError

import id.core.donate4cats.domain.CreatorProfile
import id.core.donate4cats.domain.Member

import java.time.LocalDateTime

class CreatorServiceLive[F[_]: Async](
  xa: Transactor[F]
) extends CreatorService[F] {

  override def getBydId(id: String): F[Option[CreatorProfile]] =
    for
      res <- CreatorQuery.getById(id).option.transact(xa).attempt
      _   <- when(res.isLeft) throwError {
        val exc = res.left.toOption.get
        val msg = exc.getMessage()
        new RuntimeException(s"DB Error: $msg", exc)
      }
    yield res.toOption.get

  override def getByUsername(username: String): F[Option[CreatorProfile]] =
    for
      res <- CreatorQuery.getByUsername(username).option.transact(xa).attempt
      _   <- when(res.isLeft) throwError {
        val exc = res.left.toOption.get
        val msg = exc.getMessage()
        new RuntimeException(s"DB Error: $msg", exc)
      }
    yield res.toOption.get

  override def create(
    member: Member, 
    username: String, 
    displayName: String, 
    bio: String
  ): F[CreateError | CreatorProfile] = 
    imperative {

      for
        currtime <- run(LocalDateTime.now())

        opt <- getByUsername(username.trim().toLowerCase())
        _   <- when(opt.isDefined) failWith CreateError.UsernameAlreadyTaken

        id  <- CreatorProfile.genId

        creator = CreatorProfile(
          id = id,
          memberId = member.id,
          username = username.trim().toLowerCase(),
          displayName = displayName.trim(),
          bio = bio.trim(),
          createdAt = currtime
        )

        _   <- insert(creator)

      yield creator

    }

  override def edit(
    creator: CreatorProfile, 
    displayName: String, 
    bio: String
  ): F[Unit] = 
    val updated = creator.copy(displayName = displayName, bio = bio)
    save(updated)

  override def delete(creator: CreatorProfile): F[Unit] = 
    for
      res <- CreatorQuery.delete(creator).run.transact(xa).attempt
      _   <- when(res.isLeft) throwError {
        val exc = res.left.toOption.get
        val msg = exc.getMessage()
        new RuntimeException(s"DB Error: $msg", exc)
      }
    yield ()

  private def insert(creator: CreatorProfile): F[Unit] = 
    for
      res <- CreatorQuery.insert(creator).run.transact(xa).attempt
      _   <- when(res.isLeft) throwError {
        val exc = res.left.toOption.get
        val msg = exc.getMessage()
        new RuntimeException(s"DB Error: $msg", exc)
      }
    yield ()

  private def save(creator: CreatorProfile): F[Unit] = 
    for
      res <- CreatorQuery.save(creator).run.transact(xa).attempt
      _   <- when(res.isLeft) throwError {
        val exc = res.left.toOption.get
        val msg = exc.getMessage()
        new RuntimeException(s"DB Error: $msg", exc)
      }
    yield ()
  
}
