package com.kompreneble.coupons.model

import com.kompreneble.coupons.{DuelId, TeamId}

case class DuelRates(
  duelId: DuelId,
  homeWins: Double,
  tie: Double,
  awayWins: Double,
)