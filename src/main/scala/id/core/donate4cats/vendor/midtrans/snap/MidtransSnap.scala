package id.core.donate4cats.vendor.midtrans.snap

import cats.effect.*
import cats.syntax.all.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.client.Client
import org.http4s.circe.*
import org.http4s.headers.*

import java.util.Base64

case class MidtransConfig(prod: Boolean, merchantId: String, clientKey: String, serverKey: String)

class MidtransSnap[F[_]: Async](
  conf: MidtransConfig,
  httpClient: Client[F]
) {

  private val sandboxApi = "https://app.sandbox.midtrans.com/snap/v1"
  private val prodApi = "https://app.midtrans.com/snap/v1"

  private def expectEither[A](req: Request[F])(
    implicit decoder: EntityDecoder[F, A]
  ): F[Either[String, A]] =
    httpClient.run(req).use { resp =>
      if (resp.status.isSuccess) {
        resp.as[A].attempt.map(_.leftMap(_.getMessage))
      } else {
        resp.as[String].map { body =>
          Left(s"HTTP ${resp.status.code}: $body")
        }
      }
    }

  lazy val headerAuth: String = 
    Base64.getEncoder().encodeToString(s"${conf.serverKey}:".getBytes("UTF-8"))

  def getSnap(order: Order): F[Either[String, SnapCredential]] = 
    val request = 
      Request[F](
        Method.POST, 
        Uri.fromString(s"${if conf.prod then prodApi else sandboxApi}/transactions").toOption.get
      )
      .withHeaders(Authorization(Credentials.Token(AuthScheme.Basic, headerAuth)))
      .withEntity(order.asJson)
    expectEither[SnapCredential](request)
  
}
