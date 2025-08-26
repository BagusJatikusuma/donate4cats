package id.core.donate4cats.http.route

import cats.effect.*
import cats.syntax.all.*
import org.http4s.*
import org.http4s.dsl.Http4sDsl

import id.core.donate4cats.util.syntax.monad.*

import id.core.donate4cats.domain.CreatorProfile

import id.core.donate4cats.service.CreatorService
import id.core.donate4cats.service.CreatorService.CreateError
import id.core.donate4cats.service.SessionStore.SessionData

import id.core.donate4cats.http.dto.MessageRes
import id.core.donate4cats.http.dto.CreateCreatorReq
import id.core.donate4cats.http.dto.EditCreatorReq

class CreatorRoute[F[_]: Async](
  creatorService: CreatorService[F]
) extends Http4sDsl[F] {
  
  val privateRoutes: AuthedRoutes[SessionData, F] = AuthedRoutes.of {

    case ctx @ POST -> Root / "creator" as session => 
      for
        payload   <- ctx.req.as[CreateCreatorReq]
        result    <- creatorService.create(session.payload, payload.username, payload.displayName, payload.bio)
        response  <- result match
          case CreateError.UsernameAlreadyTaken => 
            BadRequest(MessageRes("Username already taken. choose another one"))

          case creator: CreatorProfile => Created(creator)
        
      yield response

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
          _       <- when(opt.isEmpty) finishWith NotFound(MessageRes("Creator does not exist"))

          creator = opt.get

          _         <- creatorService.delete(creator)
          response  <- Ok(MessageRes("Creator deleted successfully"))

        yield response

      }

  }

}
