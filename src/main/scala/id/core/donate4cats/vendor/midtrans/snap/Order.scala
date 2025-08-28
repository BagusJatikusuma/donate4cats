package id.core.donate4cats.vendor.midtrans.snap

import cats.effect.*
import io.circe.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*

final case class Customer(
  firstName: String,
  lastName: String,
  email: String,
  phone: Option[String]
)

object Customer:
  given Encoder[Customer] with
    final def apply(c: Customer): Json = Json.obj (
      "first_name" -> Json.fromString(c.firstName),
      "last_name" -> Json.fromString(c.lastName),
      "email" -> Json.fromString(c.email),
      ("phone", if c.phone.isDefined then Json.fromString(c.phone.get) else Json.Null)
    )

  given [F[_]: Concurrent]: EntityEncoder[F, Customer] = jsonEncoderOf

final case class OrderTransaction(
  orderId: String,
  grossAmount: Double
)

object OrderTransaction:
  given Encoder[OrderTransaction] with
    final def apply(t: OrderTransaction): Json = Json.obj (
      "order_id" -> Json.fromString(t.orderId),
      "gross_amount" -> Json.fromDoubleOrNull(t.grossAmount)
    )

  given [F[_]: Concurrent]: EntityEncoder[F, OrderTransaction] = jsonEncoderOf

final case class Order(
  transactionDetails: OrderTransaction,
  customerDetails: Customer
)

object Order:
  given Encoder[Order] with
    final def apply(o: Order): Json = Json.obj(
      "transaction_details" -> o.transactionDetails.asJson,
      "customer_details" -> o.customerDetails.asJson
    )

  given [F[_]: Concurrent]: EntityEncoder[F, Order] = jsonEncoderOf
