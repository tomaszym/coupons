package com.kompreneble.coupons.model

import com.kompreneble.coupons.{DuelId, TeamId}

case class Bet(
  duelId: DuelId,
  winner: Option[TeamId],
  rate: Double,
)