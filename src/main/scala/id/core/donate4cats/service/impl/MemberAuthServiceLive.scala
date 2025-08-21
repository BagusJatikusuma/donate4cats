package id.core.donate4cats.service.impl

import cats.effect.*
import cats.syntax.all.*
import doobie.*
import doobie.implicits.*

import id.core.donate4cats.util.syntax.monad.*

import id.core.donate4cats.domain.Member
import id.core.donate4cats.domain.Member.Email
import id.core.donate4cats.domain.Member.Name
import id.core.donate4cats.domain.Member.Password

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
        _       <- when(isValid) failWith AuthError.WrongPassword

      yield member

    }

  override def basicRegister(
    name: Name, 
    email: Email, 
    password: Password
  ): F[RegistrationError | Member] = 
    imperative {

      for
        memberOpt <- getMemberByEmail(email)
        _         <- when(memberOpt.isDefined) failWith RegistrationError.EmailUsed

        memberId <- Member.generateId
        member   =  Member(id = memberId, name = name, email = email, createdAt = LocalDateTime.now())

        hashedPass <- runBlock(BCrypt.withDefaults().hashToString(8, password.asString.toCharArray))

        res   <-  ( for
                      _ <- MemberQuery.insert(member).run
                      _ <- MemberQuery.updatePassword(member, hashedPass).run
                    yield ()
                  ).transact(xa).attempt
        _     <- when(res.isLeft) throwError new RuntimeException(s"DB error: ${res.left.toOption.get.getMessage()}", res.left.toOption.get)
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

  override def resetPassword(member: Member, password: Member.Password): F[Unit] =
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
      memberOpt <- if memberRes.isLeft 
                      then throw new RuntimeException(s"DB Error ${memberRes.left.toOption.get.getMessage()}", memberRes.left.toOption.get)
                      else memberRes.toOption.get.pure[F]
    yield memberOpt

  private def getMemberPassword(member: Member): F[String] =
    for
      passRes <- MemberQuery.getPassword(member).option.transact(xa).attempt
      passOpt <- if passRes.isLeft 
                    then throw new RuntimeException(s"DB Error ${passRes.left.toOption.get.getMessage()}", passRes.left.toOption.get) 
                    else passRes.toOption.get.pure[F]
    yield passOpt.get

  
}
