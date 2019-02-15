package com.kompreneble.coupons.model

import com.kompreneble.coupons.{DuelId, TeamId}

case class Bid(
  duelId: DuelId,
  winner: Option[TeamId],
  rate: Double,
)

//object Bid {
//
//  sealed abstract class BidResult
//  case object TieBidResult extends BidResult
//  case class WinnerBidResult(
//    winner: TeamId,
//  ) extends BidResult
//
//}
