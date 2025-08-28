package id.core.donate4cats.service.query

import doobie.*
import doobie.implicits.*
import doobie.implicits.javatimedrivernative.*

import id.core.donate4cats.service.MidtransService.MidtransSession

object MidtransQuery {

  def getSessionByOrderId(orderId: String): Query0[MidtransSession] =
    sql"""
    SELECT order_id, token, redirect_url, creator_id, created_at FROM midtrans_sessions WHERE order_id = $orderId
    """
    .query
  
  def saveSession(session: MidtransSession): Update0 =
    sql"""
    INSERT INTO midtrans_sessions(order_id, token, redirect_url, creator_id, created_at) 
    VALUES(${session.orderId}, ${session.snap.token}, ${session.snap.redirectUrl}, ${session.creatorId}, ${session.createdAt})
    """
    .update

}
