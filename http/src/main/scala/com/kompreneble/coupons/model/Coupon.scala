package com.kompreneble.coupons.model

import cats.data.NonEmptyList
import com.kompreneble.coupons.{CouponId, UserId}
import cats.syntax.all._
import cats.instances.double._

case class Coupon(
  couponId: CouponId,
  userId: UserId,
  bets: NonEmptyList[Bet]
) {
  def rate: Double = bets.map(_.rate).combineAll
}