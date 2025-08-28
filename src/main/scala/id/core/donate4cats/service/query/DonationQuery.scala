package id.core.donate4cats.service.query

import doobie.*
import doobie.implicits.*
import doobie.implicits.javatimedrivernative._

import id.core.donate4cats.domain.Donation

object DonationQuery {

  def insert(donation: Donation): Update0 =
    sql"""
    INSERT INTO donations (id, creator_id, amount, message, donatur_name, donatur_email, created_at) 
    VALUES(${donation.id}, ${donation.creatorId}, ${donation.amount}, ${donation.message}, ${donation.donatur.name}, ${donation.donatur.email}, ${donation.createdAt})
    """
    .update

}
