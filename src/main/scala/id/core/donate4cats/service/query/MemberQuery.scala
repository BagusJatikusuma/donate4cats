package id.core.donate4cats.service.query

import doobie.*
import doobie.implicits.*
import doobie.implicits.javatimedrivernative._
import neotype.interop.doobie.given

import id.core.donate4cats.domain.*
import id.core.donate4cats.service.MemberAuth.ResetPasswordCred

object MemberQuery {
  
  def getMemberById(id: Member.Id): Query0[Member] =
    sql"""
    SELECT id, name, email, created_at FROM members WHERE id = $id
    """
    .query[Member]

  def getMemberByEmail(email: Email): Query0[Member] =
    sql"""
    SELECT id, name, email, created_at FROM members WHERE email = $email
    """
    .query[Member]

  def getPassword(member: Member): Query0[String] =
    sql"""
    select password FROM members WHERE id = ${member.id}
    """
    .query[String]

  def init(member: Member, password: String): Update0 =
    sql"""
    INSERT INTO members(id, name, email, password, created_at) VALUES (${member.id}, ${member.name}, ${member.email}, $password, ${member.createdAt})
    """
    .update

  def insert(member: Member): Update0 = 
    sql"""
    INSERT INTO members(id, name, email, created_at) VALUES(${member.id}, ${member.name}, ${member.email}, ${member.createdAt})
    """
    .update

  def updatePassword(member: Member, password: String): Update0 = 
    sql"""
    UPDATE members SET password = $password WHERE id = ${member.id}
    """
    .update

  def saveResetPasswordCred(member: Member, cred: ResetPasswordCred): Update0 =
    sql"""
    INSERT INTO member_reset_password(id_member, token, created_at, exp_time) VALUES(${member.id}, ${cred.token}, ${cred.createdAt}, ${cred.exp})
    """
    .update

  def getResetPasswordCred(token: String): Query0[ResetPasswordCred] = 
    sql"""
    SELECT id_member, token, created_at, exp_time FROM member_reset_password WHERE token = $token
    """
    .query[ResetPasswordCred]

}
