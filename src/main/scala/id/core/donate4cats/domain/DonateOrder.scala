package id.core.donate4cats.domain

import cats.*
import cats.syntax.all.*
import neotype.Newtype
import neotype.interop.cats.given
import java.time.LocalDateTime

final case class DonateOrder(
  orderId: DonateOrder.Id,
  member: Member.Id,
  amount: Double,
  createdAt: LocalDateTime
)

object DonateOrder:

  type Id = Id.Type
  object Id extends Newtype[String]:
    override def validate(input: String): Boolean | String = 
      if input.isBlank() then "Order id should not be empty"
      else true

    extension (id: Id)
      def asString: String = id.show
