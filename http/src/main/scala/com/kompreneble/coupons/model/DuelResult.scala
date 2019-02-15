package com.kompreneble.coupons.model

import enumeratum.EnumEntry.Snakecase
import enumeratum._

sealed trait DuelResult extends EnumEntry with Snakecase

object DuelResult extends Enum[DuelResult] {

  val values = findValues

  case object HomeTeamWon extends DuelResult
  case object Tied extends DuelResult
  case object AwayTeamWon extends DuelResult

}