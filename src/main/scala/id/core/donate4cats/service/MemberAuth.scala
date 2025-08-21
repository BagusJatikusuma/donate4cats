package id.core.donate4cats.service

import id.core.donate4cats.domain.Member
import java.time.LocalDateTime

trait MemberAuth[F[_]] {

  def basicAuth(email: Member.Email, password: Member.Password): F[MemberAuth.AuthError | Member]
  
  def basicRegister(
    name: Member.Name,
    email: Member.Email,
    password: Member.Password
  ): F[MemberAuth.RegistrationError | Member]

  def forgotPassword(member: Member): F[MemberAuth.ResetPasswordCred]

  def getResetPasswordCred(token: String): F[Option[MemberAuth.ResetPasswordCred]]

  def validateResetPasswordCred(cred: MemberAuth.ResetPasswordCred): F[MemberAuth.ResetPasswordCredInvalidity | Unit]

  def resetPassword(member: Member, password: Member.Password): F[Unit]

}

object MemberAuth:

  enum AuthError: 
    case WrongPassword, UserNotFound

  enum RegistrationError:
    case EmailUsed

  case class ResetPasswordCred(memberId: Member.Id, token: String, createdAt: LocalDateTime, exp: LocalDateTime)
  
  enum ResetPasswordCredInvalidity:
    case TokenExpired
