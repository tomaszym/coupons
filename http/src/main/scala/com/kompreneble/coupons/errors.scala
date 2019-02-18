package com.kompreneble.coupons

object errors {

  case object DuelIdMismatch extends ServiceError(500, "Duel id mismatch")
  case object TeamIdMismatch extends ServiceError(500, "TeamId mismatch")
  case object MissingRates extends ServiceError(404, "Missing rates")

  case object DuelNotFound extends ServiceError(404, "Duel not found")
  case object UnacceptableRate extends ServiceError(400, "Uncacceptable rate")
}
