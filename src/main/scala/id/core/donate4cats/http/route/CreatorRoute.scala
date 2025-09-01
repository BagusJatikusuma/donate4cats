package id.core.donate4cats.http.route

import cats.effect.*
import cats.syntax.all.*
import io.circe.Encoder
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import org.http4s.multipart.Multipart
import org.typelevel.ci.*

import id.core.donate4cats.util.syntax.monad.*

import id.core.donate4cats.domain.CreatorProfile

import id.core.donate4cats.service.CreatorService
import id.core.donate4cats.service.CreatorService.CreateError
import id.core.donate4cats.service.CreatorStorage
import id.core.donate4cats.service.SessionStore.SessionData
import id.core.donate4cats.service.SessionStore.SessionToken

import id.core.donate4cats.http.dto.MessageRes
import id.core.donate4cats.http.dto.CreateCreatorReq
import id.core.donate4cats.http.dto.EditCreatorReq

class CreatorRoute[F[_]: Async](
  creatorService: CreatorService[F],
  creatorStorage: CreatorStorage[F]
) extends Http4sDsl[F] {

  implicit def listEntityEncoder[A: Encoder]: EntityEncoder[F, List[A]] = jsonEncoderOf
  
  val privateRoutes: AuthedRoutes[(SessionToken, SessionData), F] = AuthedRoutes.of {

    case GET -> Root / "creator" / "data" as session =>
      for
        creators <- creatorService.getByMember(session._2.payload)
        response <- Ok(creators)
      yield response

    case ctx @ POST -> Root / "creator" as session => 
      ctx.req.decode[Multipart[F]] { m => 
        imperative {

          for
            convRes   <- CreateCreatorReq.fromMultipart(m)
            _         <- when(convRes.isLeft) finishWith {
              val msg = convRes.left.toOption.get
              Response(Status.BadRequest).withEntity(MessageRes(msg))
            }

            payload = convRes.toOption.get

            result    <- creatorService.create(session._2.payload, payload.username, payload.displayName, payload.bio, payload.file)
            response  <- result match
              case CreateError.UsernameAlreadyTaken => 
                BadRequest(MessageRes("Username already taken. choose another one"))

              case creator: CreatorProfile => Created(creator)
          yield response 

        } 
      }

    case ctx @ PUT -> Root / "creator" as session => 
      imperative {

        for
          payload <- ctx.req.as[EditCreatorReq]
          opt     <- creatorService.getBydId(payload.creatorId)
          _       <- when(opt.isEmpty) finishWithM NotFound("Creator does not exist")

          creator = opt.get

          _         <- creatorService.edit(creator, payload.displayName, payload.bio)
          response  <- Ok(MessageRes("Creator edited successfully"))

        yield response

      }

    case ctx @ DELETE -> Root / "creator" as session => 
      imperative {

        for
          payload <- ctx.req.as[EditCreatorReq]
          opt     <- creatorService.getBydId(payload.creatorId)
          _       <- when(opt.isEmpty) finishWithM NotFound(MessageRes("Creator does not exist"))

          creator = opt.get

          _         <- creatorService.delete(creator)
          response  <- Ok(MessageRes("Creator deleted successfully"))

        yield response

      }

  }

  val publicRoutes = HttpRoutes.of[F] {

    case GET -> Root / "creator" / creatorId =>
      imperative {
        for
          opt   <- creatorService.getBydId(creatorId)
          _     <- when(opt.isEmpty) finishWithM NotFound(MessageRes("Creator does not exist"))

          creator = opt.get

          response <- Ok(creator)
        yield response
      }
    
    case GET -> Root / "creator" / creatorId / "photo" =>
      imperative {
        for
          opt   <- creatorService.getBydId(creatorId)
          _     <- when(opt.isEmpty) finishWithM NotFound(MessageRes("Creator does not exist"))

          creator = opt.get

          response <- Ok(
            fs2.io.readInputStream(fis = creatorStorage.getPhoto(creator), chunkSize = 8192, closeAfterUse = true),
            Header.Raw(ci"Content-Type", "image/jpg")
          )
        yield response
      }


  }

}
