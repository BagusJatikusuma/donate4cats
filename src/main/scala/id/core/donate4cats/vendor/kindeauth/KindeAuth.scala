package id.core.donate4cats.vendor.kindeauth

import cats.effect.*
import cats.syntax.all.*
import io.circe.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.client.Client

case class KindeConfig(clientId: String, clientSecret: String, redirectUri: String)

case class AccessCredential(
  access_token: String
) derives Decoder

object AccessCredential:
  given [F[_]: Concurrent]: EntityDecoder[F, AccessCredential] = jsonOf

class KindeAuth[F[_]: Async](
  config: KindeConfig,
  httpClient: Client[F]
) {

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
  
  def getAccessCredential(callbackCode: String): F[Either[String, AccessCredential]] = 
    val form: UrlForm = UrlForm(
      "client_id"     -> config.clientId,
      "client_secret" -> config.clientSecret,
      "grant_type"    -> "authorization_code",
      "redirect_uri"  -> config.redirectUri,
      "code"          -> callbackCode 
    )
    val req = 
      Request[F](
        Method.POST,
        Uri.fromString(s"https://circlestreamid.kinde.com/oauth2/token").toOption.get
      )
      .withEntity(form)
    expectEither[AccessCredential](req)

}
