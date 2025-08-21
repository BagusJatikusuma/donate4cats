package id.core.donate4cats.service.impl

import cats.effect.*
import cats.syntax.all.*
import doobie.*
import doobie.implicits.*

import id.core.donate4cats.util.syntax.monad.*

import id.core.donate4cats.domain.Member.Email
import id.core.donate4cats.domain.Member
import id.core.donate4cats.service.MemberService
import id.core.donate4cats.service.query.MemberQuery

class MemberServiceLive[F[_]: Concurrent](
  xa: Transactor[F]
) extends MemberService[F] {

  override def getById(id: Member.Id): F[Option[Member]] =
    for
      res <- MemberQuery.getMemberById(id).option.transact(xa).attempt
      _   <- when(res.isLeft) throwError {
        val exc = res.left.toOption.get
        val msg = exc.getMessage()
        new RuntimeException(s"DB Error: $msg", exc)
      }
    yield res.toOption.get

  override def getByEmail(email: Email): F[Option[Member]] = 
    for
      res <- MemberQuery.getMemberByEmail(email).option.transact(xa).attempt
      _   <- when(res.isLeft) throwError {
        val exc = res.left.toOption.get
        val msg = exc.getMessage()
        new RuntimeException(s"DB Error: $msg", exc)
      }
    yield res.toOption.get


}
