package id.core.donate4cats.http.route

import cats.effect.*
import cats.syntax.all.*
import org.http4s.*
import org.http4s.dsl.Http4sDsl

import id.core.donate4cats.service.BankAccountService
import id.core.donate4cats.service.SessionStore.SessionToken
import id.core.donate4cats.service.SessionStore.SessionData

class BankAccountRoute[F[_]: Async](
  bankAccountService: BankAccountService[F]
) extends Http4sDsl[F] {

  val protectedRoutes: AuthedRoutes[(SessionToken, SessionData), F] = AuthedRoutes.of {

    case GET -> Root / "bank-accounts" as session => 
      for 
        bankAccounts <- bankAccountService.getByMember(session._2.payload)
        _            <- Async[F].delay(println(bankAccounts))
        resp  <- Ok("Unimplemented")
      yield resp 

  }
  
}
