package id.core.donate4cats.service

import id.core.donate4cats.domain.*

trait DonationService[F[_]] {

  def makeDonation(
    donatur: Donatur,
    message: String,
    creator: CreatorProfile,
    amount: Double 
  ): F[Donation]
  
}


