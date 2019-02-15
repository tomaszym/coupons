package com.kompreneble.coupons.view

import java.util.UUID

import cats.data.NonEmptyMap
import com.kompreneble.coupons.DuelId
import com.kompreneble.coupons.model.Bid
import io.circe._
import cats.implicits._
import io.circe.syntax._
import io.circe.derivation._

case class CouponRequest(
  bids: NonEmptyMap[DuelId, Bid],
)
object CouponRequest {


  implicit val uuidDecoder: Decoder[UUID] = Decoder.decodeString.map(UUID.fromString)
  implicit val uuidKeyDecoder: KeyDecoder[UUID] = KeyDecoder.decodeKeyString.map(UUID.fromString)
  implicit val bidDecoder: Decoder[Bid] = deriveDecoder
  implicit val decoder: Decoder[CouponRequest] = deriveDecoder

}