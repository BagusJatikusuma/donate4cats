package id.core.donate4cats.service

import id.core.donate4cats.domain.Member
import id.core.donate4cats.domain.BankAccount

trait BankAccountService[F[_]] {

  def getByMember(member: Member): F[List[BankAccount]]

  def getById(id: BankAccount.Id): F[Option[BankAccount]]

  def add(
    member: Member, 
    bank: String, 
    accountNumber: BankAccount.Number
  ): F[BankAccount]

  def remove(
    bankAccount: BankAccount
  ): F[Unit]
  
}
