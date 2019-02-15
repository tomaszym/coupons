package com.kompreneble.coupons

import java.util.UUID

import cats.effect.{Concurrent, ConcurrentEffect}
import cats.mtl.ApplicativeAsk
import com.kompreneble.coupons.model.{Bid, Coupon, Duel}
import com.kompreneble.coupons.view.CouponRequest
import io.chrisdavenport.log4cats.{Logger, MessageLogger, StructuredLogger}
import cats._
import cats.data._
import cats.implicits._
import com.kompreneble.coupons.CouponsService.BidRow
import doobie._
import doobie.implicits._
import io.chrisdavenport.fuuid.doobie.implicits._
import doobie.postgres.implicits._

class CouponsService[F[_]](
  duels: DuelsService[F],
  rates: RatesService[F],
)(implicit
  DB: Transactor[F],
  L: StructuredLogger[F],
  E: Concurrent[F],
  Ctx: ApplicativeAsk[F, RequestContext],
) {

  def createCoupon(
    userId: UserId,
    request: CouponRequest
  ): F[CouponId] = {

    request.bids.toList

    val couponId = UUID.randomUUID()

    for {
      _ <- sql"insert into coupons (coupon_id, user_id) values ($couponId, $userId)".update.run
      bidUpdate = "insert into bids (coupon_id, duel_id, winner, rate) values ($couponId,?,?,?)"
      _ <- Update[Bid](bidUpdate).updateMany(request.bids.toList)
    } yield ()


    ???
  }

  def getOfUser(userId: UserId): F[List[Coupon]] = {

    val query = sql"""SELECT
         |    coupons.coupon_id, coupons.user_id, bids.duel_id, bids.winner, bids.rate
         |FROM
         |    coupons NATURAL JOIN bids
         |WHERE
         |    coupons.user_id = $userId""".stripMargin.query[(CouponId, BidRow)]

    for {
      ctx <- Ctx.ask
      _ <- L.debug(ctx.logMarker:_*)(query.sql)
      result <- query.stream.compile.toList.transact(DB)
    } yield result.groupByNel(_._1).mapValues(_.map(_._2)).map {
      case (couponId, bidRows) =>
        Coupon(couponId, userId, bidRows.map(_.bid))
    }.toList

  }

}


object CouponsService {

  case class CouponRow(coupon_id: CouponId, user_id: UserId)

  case class BidRow(duel_id: DuelId, winner: Option[TeamId], rate: Double, coupon_id: CouponId) {
    def bid: Bid = Bid(duel_id, winner, rate)
  }



}