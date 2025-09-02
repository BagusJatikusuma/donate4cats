package id.core.donate4cats.http.route

import cats.effect.*
import cats.syntax.all.*
import org.http4s.*
import org.http4s.dsl.Http4sDsl
import org.http4s.scalatags.*

import id.core.donate4cats.domain.Member
import id.core.donate4cats.service.MemberService
import id.core.donate4cats.service.MemberAuth
import id.core.donate4cats.service.MemberAuth.RegistrationError
import id.core.donate4cats.service.MemberAuth.ResetPasswordCredInvalidity
import id.core.donate4cats.service.SessionStore
import id.core.donate4cats.service.SessionStore.SessionData
import id.core.donate4cats.http.middleware.CookieAuthMiddleware
import id.core.donate4cats.http.dto.SigninReq
import id.core.donate4cats.http.dto.SignupReq
import id.core.donate4cats.http.dto.MessageRes
import id.core.donate4cats.http.dto.ForgotPasswordReq
import id.core.donate4cats.http.dto.ResetPasswordReq

import id.core.donate4cats.http.view.SigninPage
import id.core.donate4cats.http.view.SignupPage
import id.core.donate4cats.http.view.Homepage
import id.core.donate4cats.http.view.MemberHomePage

import id.core.donate4cats.util.syntax.monad.*
import id.core.donate4cats.service.SessionStore.SessionToken

final class MemberRoute[F[_]: Async](
  memberService: MemberService[F],
  memberAuth: MemberAuth[F],
  sessionStore: SessionStore[F]
) extends Http4sDsl[F] {

  val protectedRoutes: AuthedRoutes[(SessionToken, SessionData), F] = AuthedRoutes.of {

    case GET -> Root / "home" as session =>
      Ok(MemberHomePage.index(session._2.payload))

    case GET -> Root / "profile" as session =>
      val user = session._2.payload
      Ok(user)

    case req @ PUT -> Root / "profile" as session =>
      Ok("Unimplemented....")

    case PUT -> Root / "logout" as session =>
      for
        _     <- sessionStore.delete(session._1)
        resp  <- Ok(MessageRes("success logout"))
      yield resp.addCookie(CookieAuthMiddleware.removeSessionCookie)

  }
  
  val publicRoutes = HttpRoutes.of[F] {

    case req @ GET -> Root =>
      req.cookies.find(_.name == "session_id") match
        case None => Ok(Homepage.index())
        case Some(cookie) =>
          val sessId = cookie.content
          for
            sessionOpt <- sessionStore.get(SessionToken.make(sessId).toOption.get)
            resp  <- sessionOpt match
              case None => 
                Ok(Homepage.index())
              
              case Some(session) =>
                Ok(Homepage.index(Some(session.payload)))
          yield resp
      

    case GET -> Root / "signin" =>
      Ok(SigninPage.index())

    case req @ POST -> Root / "signin" => 
      for
        payload   <- req.as[SigninReq]
        result    <- memberAuth.basicAuth(payload.email, payload.password)
        response  <- result match {

          case MemberAuth.AuthError.UserNotFound => 
            Response(Status.Unauthorized).withEntity(MessageRes("Member or password does not match")).pure[F]

          case MemberAuth.AuthError.WrongPassword => 
            Response(Status.Unauthorized).withEntity(MessageRes("Member or password does not match")).pure[F]

          case user: Member => 
            val ttlSeconds = 3600L
            val expiresAt  = System.currentTimeMillis() + ttlSeconds * 1000
            val session    = SessionData("user123", expiresAt, user)
            for 
              token <- sessionStore.create(session)
              cookie = CookieAuthMiddleware.makeSessionCookie(token, ttlSeconds)
              resp  <- Ok(user)
            yield resp.addCookie(cookie)

        }
      yield response

    case GET -> Root / "signup" =>
      Ok(SignupPage.index())

    case req @ POST -> Root / "signup" =>
      for 
        payload   <- req.as[SignupReq]
        result    <- memberAuth.basicRegister(payload.name, payload.email, payload.password)
        
        response  <- result match
          case RegistrationError.EmailUsed => 
            BadRequest("Email already used")

          case member: Member => Ok(member)
          
      yield response

    case req @ POST -> Root / "forgot-password" =>
      imperative {
        for
          payload   <- req.as[ForgotPasswordReq]
          memberOpt <- memberService.getByEmail(payload.email)
          _         <- when(memberOpt.isEmpty) finishWithM NotFound(MessageRes("Member with this email does not exist"))
          member    =  memberOpt.get

          resetCred <- memberAuth.forgotPassword(member)
          response  <- Ok(MessageRes("Reset token has been created"))
        yield response
      }

    case req @ POST -> Root / "reset-password" =>
      imperative {
        for
          payload   <- req.as[ResetPasswordReq]

          credOpt   <- memberAuth.getResetPasswordCred(payload.token)
          _         <- when(credOpt.isEmpty) finishWithM NotFound(MessageRes("Token does not exist"))
          resetCred =  credOpt.get

          memberOpt <- memberService.getById(resetCred.memberId)
          _         <- when(memberOpt.isEmpty) throwError new IllegalStateException("Member from reset password credential does not exist")
          member    =  memberOpt.get

          valRes    <- memberAuth.validateResetPasswordCred(resetCred)
          _         <- valRes match
            case ResetPasswordCredInvalidity.TokenExpired => finishWith(NotFound(MessageRes("Token has been expired")))
            case _   => continue
          
          _         <- memberAuth.resetPassword(member, payload.newPassword)
          response  <- Ok(MessageRes("Reset password Succeed"))
        yield response
      }

  }

}
