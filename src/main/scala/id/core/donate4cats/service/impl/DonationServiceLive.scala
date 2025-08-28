package id.core.donate4cats.service.impl

import cats.effect.*
import cats.syntax.all.*
import doobie.*
import doobie.implicits.*

import id.core.donate4cats.domain.CreatorProfile
import id.core.donate4cats.domain.Donation
import id.core.donate4cats.domain.Donatur

import id.core.donate4cats.service.DonationService
import id.core.donate4cats.service.query.DonationQuery

import id.core.donate4cats.util.syntax.monad.*

import java.time.LocalDateTime

final class DonationServiceLive[F[_]: Async](
  xa: Transactor[F]
) extends DonationService[F] {

  override def makeDonation(
    donateId: String, 
    donatur: Donatur, 
    message: String, 
    creator: CreatorProfile, 
    amount: Double
  ): F[Donation] =
    for
      currtime <- run(LocalDateTime.now())
      donation =  Donation(donateId, creator.id, amount, message, donatur, currtime)
      _        <- add(donation)
    yield donation

  private def add(donation: Donation): F[Unit] = 
    for
      res   <- DonationQuery.insert(donation).run.transact(xa).attempt
      _     <- when(res.isLeft) throwError {
        val exc = res.left.toOption.get
        val msg = exc.getMessage()

        new RuntimeException(s"DB Error: $msg", exc)
      }
    yield ()
  
}
