package id.core.donate4cats.http.dto

import MidtransPaymentCallback.*

final case class MidtransPaymentCallback(
  orderId: String,
  grossAmount: Double,
  paymentType: PaymentType,
  fraudStatus: String,
)

object MidtransPaymentCallback:
  import cats.effect.*
  import io.circe.*
  import io.circe.Decoder.Result
  import org.http4s.*
  import org.http4s.circe.*

  enum PaymentType:
    case VAPayment(vaNumber: String, bank: String)

  object PaymentType:

    case class VAPaymentType(vaNumber: String, bank: String)
    object VAPaymentType:
      given Decoder[VAPaymentType] = new Decoder:
        def apply(c: HCursor): Result[VAPaymentType] = 
          for
            num   <- c.get[String]("va_number")
            bank  <- c.get[String]("bank")
          yield VAPaymentType(num, bank)

      given[F[_]: Concurrent]: EntityDecoder[F, VAPaymentType] = jsonOf

    given Decoder[PaymentType] = new Decoder:
      def apply(c: HCursor): Result[PaymentType] = 
        for
          typeStr     <- c.get[String]("payment_type")
          paymentType <- typeStr match
            case "bank_transfer" => 
              c.get[String]("permata_va_number") match
                case Left(_) =>
                  for
                    vas <- c.get[List[VAPaymentType]]("va_numbers")
                  yield PaymentType.VAPayment(vas.head.vaNumber, vas.head.bank)
                
                case Right(vaNum) => Right(PaymentType.VAPayment(vaNum, "permata"))

            case "echannel" =>
              for
                billKey <- c.get[String]("bill_key")
              yield PaymentType.VAPayment(billKey, "mandiri")

            case _ => Left(DecodingFailure.apply("not supported yet", List()))
                
        yield paymentType

    given[F[_]: Concurrent]: EntityDecoder[F, PaymentType] = jsonOf

  given Decoder[MidtransPaymentCallback] = new Decoder:
    def apply(c: HCursor): Result[MidtransPaymentCallback] = 
      for 
        orderId     <- c.get[String]("order_id")
        amount      <- c.get[Double]("gross_amount")
        paymentType <- c.as[PaymentType]
        fraudStatus <- c.get[String]("fraud_status")
      yield MidtransPaymentCallback(orderId, amount, paymentType, fraudStatus)

  given[F[_]: Concurrent]: EntityDecoder[F, MidtransPaymentCallback] = jsonOf


// object TestJson:
//   import io.circe.parser.*

//   @main def test = 
//     val jsStr = """{ "order_id": "ODR1", "gross_amount": 10000.00, "payment_type": "bank_transfer", "va_numbers": [ { "va_number": "9888643225576761", "bank": "bni" } ], "fraud_status": "accept" }"""
//     val obj   = decode[MidtransPaymentCallback](jsStr)
//     println(obj)
