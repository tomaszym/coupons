package com.kompreneble.coupons.view

import java.util.UUID

import cats.data.{NonEmptyList, NonEmptyMap}
import com.kompreneble.coupons.DuelId
import com.kompreneble.coupons.model.Bet
import io.circe._
import cats.implicits._
import io.circe.syntax._
import io.circe.derivation._

case class CouponRequest(
  bets: NonEmptyList[Bet],
)
object CouponRequest {


  implicit val uuidDecoder: Decoder[UUID] = Decoder.decodeString.map(UUID.fromString)
  implicit val uuidKeyDecoder: KeyDecoder[UUID] = KeyDecoder.decodeKeyString.map(UUID.fromString)
  implicit val bidDecoder: Decoder[Bet] = deriveDecoder
  implicit val decoder: Decoder[CouponRequest] = deriveDecoder

}