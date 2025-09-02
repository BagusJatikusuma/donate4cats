package id.core.donate4cats.service.impl

import cats.effect.*
import cats.syntax.all.*
import doobie.*
import doobie.implicits.*

import id.core.donate4cats.domain.BankAccount
import id.core.donate4cats.domain.Member
import id.core.donate4cats.service.BankAccountService
import id.core.donate4cats.service.query.BankAccountQuery

import id.core.donate4cats.util.syntax.monad.*

import java.time.LocalDateTime

class BankAccountServiceLive[F[_]: Async](
  xa: Transactor[F]
) extends BankAccountService[F] {

  override def getByMember(member: Member): F[List[BankAccount]] = 
    for 
      res <- BankAccountQuery.getByMember(member).to[List].transact(xa).attempt
      _   <- when(res.isLeft) throwError {
        val exc = res.left.toOption.get
        val msg = exc.getMessage()
        new java.lang.RuntimeException(s"Db Err: $msg", exc)
      }
    yield res.toOption.get

  override def getById(id: BankAccount.Id): F[Option[BankAccount]] =
    for 
      res <- BankAccountQuery.getById(id).option.transact(xa).attempt
      _   <- when(res.isLeft) throwError {
        val exc = res.left.toOption.get
        val msg = exc.getMessage()
        new java.lang.RuntimeException(s"Db Err: $msg", exc)
      }
    yield res.toOption.get

  override def add(member: Member, bank: String, accountNumber: BankAccount.Number): F[BankAccount] = 
    for 
      currtime  <- runBlock(LocalDateTime.now())
      id        <- BankAccount.generateId

      account = BankAccount(id, member.id, accountNumber, bank, currtime)

      res <- BankAccountQuery.insert(account).run.transact(xa).attempt
      _   <- when(res.isLeft) throwError {
        val exc = res.left.toOption.get
        val msg = exc.getMessage()
        new java.lang.RuntimeException(s"Db Err: $msg", exc)
      }
    yield account

  override def remove(bankAccount: BankAccount): F[Unit] = 
    for
      res <- BankAccountQuery.delete(bankAccount).run.transact(xa).attempt
      _   <- when(res.isLeft) throwError {
        val exc = res.left.toOption.get
        val msg = exc.getMessage()
        new java.lang.RuntimeException(s"Db Err: $msg", exc)
      }
    yield ()

  
}
