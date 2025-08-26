package id.core.donate4cats.service

import id.core.donate4cats.domain.*

trait MemberService[F[_]] {

  def getById(id: Member.Id): F[Option[Member]]

  def getByEmail(email: Email): F[Option[Member]]

}
