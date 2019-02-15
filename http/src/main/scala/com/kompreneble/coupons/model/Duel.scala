package com.kompreneble.coupons.model

import com.kompreneble.coupons.{DuelId, TeamId}

case class Duel(
  duelId: DuelId,
  home: TeamId,
  away: TeamId,
  result: Option[DuelResult],
) {

  def teams: Set[TeamId] = Set(home,away)

}
