package com.kompreneble.coupons.view

import java.util.UUID

import com.kompreneble.coupons.CouponsService.BetRow
import com.kompreneble.coupons.DuelsService.DuelRow
import com.kompreneble.coupons.model.DuelResult
import com.kompreneble.coupons.model.DuelResult.{AwayTeamWon, HomeTeamWon, Tied}
import com.kompreneble.coupons.view.CouponView.BetView
import com.kompreneble.coupons.{CouponId, DuelId, TeamId}
import io.circe.derivation.deriveEncoder
import io.circe.{Decoder, Encoder}

case class CouponView (
  couponId: CouponId,
  bets: List[BetView],
  success: Option[Boolean],
  rate: Double,
)

object CouponView {

  case class BetView(
    duelId: DuelId,
    rate: Double,
    bet: Option[TeamId],
    success: Option[Boolean],
    result: Option[DuelResult],
    homeTeam: TeamId,
    awayTeam: TeamId,
  )

  def view(
    couponId: CouponId,
    bets: List[(BetRow, DuelRow)],
  ) = {
    val betViews = bets.map(viewBet _ tupled)
    val success = betViews.tail.foldLeft(betViews.head.success) { case (acc, bet) =>
      (acc, bet.success) match {
        case (Some(a), Some(b)) => Some(a&&b)
        case _ => None
      }
    }
    val rate = betViews.map(_.rate).product
    CouponView(
      couponId,
      betViews,
      success,
      rate,
    )
  }

  private def viewBet(bet: BetRow, duel: DuelRow): BetView = {

    val success = duel.result map {
      case HomeTeamWon => bet.winner.contains(duel.home_team)
      case AwayTeamWon => bet.winner.contains(duel.away_team)
      case Tied => bet.winner.isEmpty
    }

    BetView(
      duelId = duel.duel_id,
      rate = bet.rate,
      homeTeam = duel.home_team,
      awayTeam = duel.away_team,
      result = duel.result,
      success = success,
      bet = bet.winner,
    )
  }

  implicit val uuidDecoder: Decoder[UUID] = Decoder.decodeString.map(UUID.fromString)

  implicit val betEncoder: Encoder[BetView] = deriveEncoder
  implicit val encoder: Encoder[CouponView] = deriveEncoder
}
