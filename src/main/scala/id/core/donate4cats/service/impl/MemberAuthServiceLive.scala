package id.core.donate4cats.service.impl

import cats.effect.*
import cats.syntax.all.*
import doobie.*
import doobie.implicits.*

import id.core.donate4cats.util.syntax.monad.*

import id.core.donate4cats.domain.*

import id.core.donate4cats.service.MemberAuth
import id.core.donate4cats.service.MemberAuth.AuthError
import id.core.donate4cats.service.MemberAuth.RegistrationError
import id.core.donate4cats.service.MemberAuth.ResetPasswordCred
import id.core.donate4cats.service.MemberAuth.ResetPasswordCredInvalidity
import id.core.donate4cats.service.query.MemberQuery

import at.favre.lib.crypto.bcrypt.BCrypt
import java.time.LocalDateTime
import java.util.UUID

class MemberAuthServiceLive[F[_]: Async](
  xa: Transactor[F]
) extends MemberAuth[F] {

  override def basicAuth(
    email: Email, 
    password: Password
  ): F[AuthError | Member] = 
    imperative {

      for
        memberOpt <- getMemberByEmail(email)
        _         <- when(memberOpt.isEmpty) failWith AuthError.UserNotFound

        member = memberOpt.get

        hashedPass <- getMemberPassword(member)

        isValid <- run(BCrypt.verifyer.verify(password.asString.toCharArray, hashedPass).verified)
        _       <- whenNot(isValid) failWith AuthError.WrongPassword

      yield member

    }

  override def basicRegister(
    name: Name, 
    email: Email, 
    password: Password
  ): F[RegistrationError | Member] = 
    imperative {

      for
        currtime  <- run(LocalDateTime.now())

        memberOpt <- getMemberByEmail(email)
        _         <- when(memberOpt.isDefined) failWith RegistrationError.EmailUsed

        memberId <- Member.generateId
        member   =  Member(id = memberId, name = name, email = email, createdAt = currtime)

        hashedPass <- runBlock(BCrypt.withDefaults().hashToString(8, password.asString.toCharArray))
        res        <- MemberQuery.init(member, hashedPass).run.transact(xa).attempt

        _     <- when(res.isLeft) throwError {
          val exc = res.left.toOption.get
          new RuntimeException(s"DB error: ${exc.getMessage()}", exc)
        }
      yield member

    }

  override def forgotPassword(member: Member): F[ResetPasswordCred] = 
    for 
      currtime  <- run(LocalDateTime.now())
      token     <- run(UUID.randomUUID().toString())
      exptime   =  currtime.plusDays(3)
      cred      =  ResetPasswordCred(member.id, token, currtime, exptime)
      _         <- saveResetPasswordCred(member, cred)
    yield cred

  override def getResetPasswordCred(token: String): F[Option[ResetPasswordCred]] =
    for 
      res   <- MemberQuery.getResetPasswordCred(token).option.transact(xa).attempt
      _     <- when(res.isLeft) throwError {
        val exc = res.left.toOption.get
        val msg = exc.getMessage()
        new RuntimeException(s"DB Error: $msg", exc)
      }
    yield res.toOption.get

  override def validateResetPasswordCred(cred: ResetPasswordCred): F[ResetPasswordCredInvalidity | Unit] = 
    imperative {
      for
        currtime <- run(LocalDateTime.now())
        _        <- when(currtime.isAfter(cred.exp)) finishWith ResetPasswordCredInvalidity.TokenExpired
      yield ()
    }

  override def resetPassword(member: Member, password: Password): F[Unit] =
    for 
      hashedPass <- runBlock(BCrypt.withDefaults().hashToString(8, password.asString.toCharArray))
      res        <- MemberQuery.updatePassword(member, hashedPass).run.transact(xa).attempt
      _          <- when(res.isLeft) throwError {
        val exc = res.left.toOption.get
        val msg = exc.getMessage()
        new RuntimeException(s"DB Error: $msg", exc)
      }
    yield ()

  private def saveResetPasswordCred(member: Member, cred: ResetPasswordCred): F[Unit] = 
    for
      res   <- MemberQuery.saveResetPasswordCred(member, cred).run.transact(xa).attempt
      _     <- when(res.isLeft) throwError {
        val exc = res.left.toOption.get
        val msg = exc.getMessage()
        new RuntimeException(s"DB Error: $msg", exc)
      }
    yield ()

  private def getMemberByEmail(email: Email): F[Option[Member]] =
    for
      memberRes <- MemberQuery.getMemberByEmail(email).option.transact(xa).attempt
      _         <- when(memberRes.isLeft) throwError {
        val exc = memberRes.left.toOption.get
        val msg = exc.getMessage()
        new RuntimeException(s"DB Error $msg", exc)
      }
    yield memberRes.toOption.get

  private def getMemberPassword(member: Member): F[String] =
    for
      passRes <- MemberQuery.getPassword(member).option.transact(xa).attempt
      _       <- when(passRes.isLeft) throwError {
        val exc = passRes.left.toOption.get
        val msg = exc.getMessage()
        new RuntimeException(s"DB Error $msg", exc)
      }
      _   <- Async[F].delay(println(passRes.toOption.get.get))
    yield passRes.toOption.get.get

  
}