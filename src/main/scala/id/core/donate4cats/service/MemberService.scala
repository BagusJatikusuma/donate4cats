package id.core.donate4cats.service

import id.core.donate4cats.domain.Member

trait MemberService[F[_]] {

  def getById(id: Member.Id): F[Option[Member]]

  def getByEmail(email: Member.Email): F[Option[Member]]

}
