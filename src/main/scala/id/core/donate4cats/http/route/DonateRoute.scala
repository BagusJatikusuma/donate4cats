package id.core.donate4cats.http.route

import cats.effect.*
import cats.syntax.all.*
import org.http4s.*
import org.http4s.dsl.Http4sDsl

import id.core.donate4cats.domain.Donatur

import id.core.donate4cats.service.CreatorService
import id.core.donate4cats.service.DonationService
import id.core.donate4cats.service.MidtransService

import id.core.donate4cats.http.dto.MakeDonationReq
import id.core.donate4cats.http.dto.MessageRes
import id.core.donate4cats.http.dto.MidtransPaymentCallback

import id.core.donate4cats.util.syntax.monad.*


final class DonateRoute[F[_]: Async](
  midtransService: MidtransService[F],
  creatorService: CreatorService[F],
  donationService: DonationService[F]
) extends Http4sDsl[F] {


  val publicRoutes = HttpRoutes.of[F] {

    case req @ POST -> Root / "donation" =>
      imperative {
        
        for
          payload <- req.as[MakeDonationReq]

          donatur =  Donatur(name = payload.name, email = payload.email)

          creatorOpt <- creatorService.getBydId(payload.creatorId)
          _          <- when(creatorOpt.isEmpty) finishWith Response(Status.BadRequest).withEntity(MessageRes("Creator does not exist"))

          creator = creatorOpt.get

          session <- midtransService.initSession(donatur, payload.amount, creator)

          resp    <- Created(session)
        yield resp

      }

    case req @ POST -> Root / "midtrans-callback-handler" =>
      imperative {
        for
          payload <- req.as[MidtransPaymentCallback]

          sessOpt <- midtransService.getByOrderId(payload.orderId)
          _       <- when(sessOpt.isEmpty) finishWith Response(Status.NotFound).withEntity(MessageRes("Session does not exist"))

          session =  sessOpt.get
          
          creatorOpt <- creatorService.getBydId(session.creatorId)
          _          <- when(creatorOpt.isEmpty) throwError new RuntimeException("midtrans session creator does not exist")

          creator = creatorOpt.get

          _   <- donationService.makeDonation(session.orderId, Donatur(name = "not found lol", email = "hehe@gmail.com"), message = "forgot save to session lol", creator, payload.grossAmount)

        yield Response(Status.Ok).withEntity(MessageRes("received"))
      }

  }

  
}
