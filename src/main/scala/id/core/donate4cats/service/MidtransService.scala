package id.core.donate4cats.service

import cats.effect.*
import cats.syntax.all.*
import doobie.*
import doobie.implicits.*

import id.core.donate4cats.domain.Donatur
import id.core.donate4cats.domain.Donation
import id.core.donate4cats.domain.CreatorProfile

import id.core.donate4cats.vendor.midtrans.snap.MidtransSnap
import id.core.donate4cats.vendor.midtrans.snap.SnapCredential
import id.core.donate4cats.vendor.midtrans.snap.OrderTransaction
import id.core.donate4cats.vendor.midtrans.snap.Customer
import id.core.donate4cats.vendor.midtrans.snap.Order

import id.core.donate4cats.util.syntax.monad.*

import java.time.LocalDateTime

final class MidtransService[F[_]: Async](
  snap: MidtransSnap[F],
  xa: Transactor[F]
) {

  import MidtransService.*
  import id.core.donate4cats.service.query.MidtransQuery

  def getByOrderId(orderId: String): F[Option[MidtransSession]] =
    for
      res <- MidtransQuery.getSessionByOrderId(orderId).option.transact(xa).attempt
      _   <- when(res.isLeft) throwError {
        val exc = res.left.toOption.get
        val msg = exc.getMessage()
        new RuntimeException(s"DB Error: $msg", exc)
      }
    yield res.toOption.get
  
  def initSession(
    donatur: Donatur,
    amount: Double,
    creator: CreatorProfile
  ): F[MidtransSession] =
    for
      donationId <- Donation.genId
      currtime   <- run(LocalDateTime.now())
      
      orderTx = OrderTransaction(donationId, amount)
      cust    = Customer(donatur.name, "", donatur.email, None)
      order   = Order(orderTx, cust)

      result  <- snap.getSnap(order)
      _       <- when(result.isLeft) throwError new RuntimeException(result.left.toOption.get)

      session =  MidtransSession(orderId = donationId, snap = result.toOption.get, creatorId = creator.id, createdAt = currtime)
      _       <- saveSession(session)

    yield session

  private def saveSession(session: MidtransSession): F[Unit] = 
    for
      res <- MidtransQuery.saveSession(session).run.transact(xa).attempt
      _   <- when(res.isLeft) throwError {
        val exc = res.left.toOption.get
        val msg = exc.getMessage()
        new RuntimeException(s"DB Error: $msg", exc)
      }
    yield ()

}

object MidtransService:

  import io.circe.*
  import org.http4s.*
  import org.http4s.circe.*

  case class MidtransSession(
    orderId: String, 
    snap: SnapCredential,
    creatorId: String, 
    createdAt: LocalDateTime
  ) derives Encoder

  given [F[_]: Concurrent]: EntityEncoder[F, MidtransSession] = jsonEncoderOf

