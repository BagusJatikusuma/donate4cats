package id.core.donate4cats.http.middleware

import cats.data.*
import cats.effect.*
import cats.syntax.all.*
import io.circe.*
import org.http4s.*
import org.http4s.dsl.Http4sDsl

object ExceptionMiddleware:

  def extractCirceError(t: Throwable): Option[DecodingFailure] =
    t match
      case imb: InvalidMessageBodyFailure =>
        imb.cause match
          case Some(df: DecodingFailure) => Some(df)
          case Some(other) => extractCirceError(other)
          case None => None
      case df: DecodingFailure => Some(df)
      case _ => None

  def catchMiddleware[F[_]: Sync](routes: HttpApp[F]): HttpApp[F] = Kleisli { req =>
    val dsl = new Http4sDsl[F]{}
    import dsl.*
    
    routes(req).handleErrorWith { throwable =>
      extractCirceError(throwable) match 
        case Some(df) =>
          val errorMsg = s"${df.message}"
          BadRequest(errorMsg)
        case None => 
          throwable.printStackTrace()
          InternalServerError(s"Unexpected error: $throwable")
    }

  }
