package id.core.donate4cats.service.query

import doobie.*
import doobie.implicits.*
import doobie.implicits.javatimedrivernative.*
import neotype.interop.doobie.given

import id.core.donate4cats.domain.BankAccount
import id.core.donate4cats.domain.Member

object BankAccountQuery {
  
  private def selectFr: Fragment =
    fr"""
    SELECT id, member_id, account_number, bank, created_at FROM bank_accounts
    """

  def getByMember(member: Member): Query0[BankAccount] =
    val sql = selectFr ++ fr"WHERE member_id = ${member.id}"
    sql.query[BankAccount]

  def getById(id: BankAccount.Id): Query0[BankAccount] =
    val sql = selectFr ++ fr"WHERE id = $id"
    sql.query[BankAccount]

  def insert(bankAccount: BankAccount): Update0 =
    sql"""
    INSERT INTO bank_accounts(id, member_id, account_number, bank, created_at)
    VALUES(${bankAccount.id}, ${bankAccount.memberId}, ${bankAccount.accountNumber}, ${bankAccount.bank}, ${bankAccount.createdAt})
    """
    .update

  def delete(bankAccount: BankAccount): Update0 =
    sql"""DELETE FROM bank_accounts WHERE id = ${bankAccount.id}"""
    .update

}
