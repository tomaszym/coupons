package com.kompreneble.coupons

import java.util.UUID

import cats.effect.{Concurrent, ConcurrentEffect}
import cats.mtl.ApplicativeAsk
import com.kompreneble.coupons.model.{Bet, Coupon, Duel, DuelResult}
import com.kompreneble.coupons.view.{CouponRequest, CouponView}
import io.chrisdavenport.log4cats.{Logger, MessageLogger, StructuredLogger}
import cats._
import cats.data._
import cats.implicits._
import com.kompreneble.coupons.CouponsService.BetRow
import com.kompreneble.coupons.DuelsService.DuelRow
import doobie._
import doobie.implicits._
import io.chrisdavenport.fuuid.doobie.implicits._
import doobie.postgres.implicits._
import shapeless.syntax.std.tuple._
import DuelsService.DuelResultMeta
import com.kompreneble.coupons.errors.{DuelNotFound, UnacceptableRate}

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

    val couponId = UUID.randomUUID()

    val update: ConnectionIO[Unit] = for {
      _ <- sql"insert into coupons (coupon_id, user_id) values ($couponId, $userId)".update.run
      betUpdate = s"insert into bets (coupon_id, duel_id, winner, rate) values ('$couponId',?,?,?)"
      _ <- Update[Bet](betUpdate).updateMany(request.bets.toList)
    } yield ()

    for {
      _ <- duels.view.ensure(DuelNotFound) {
        duels =>
          request.bets.forall(b => duels.exists(_.duelId == b.duelId))
      }

      _ <- duels.view.ensure(UnacceptableRate) {
        duels =>
          request.bets.forall(b => duels.find(_.duelId == b.duelId).exists {
            duel =>

              b.winner match {
                case Some(teamId) => duel.winnerRates.get(teamId).contains(b.rate)
                case None =>  duel.tieRate == b.rate
              }
          })
      }
      _ <- update.transact(DB)
    } yield couponId
  }

  def view(userId: UserId): F[List[CouponView]] = {

    val query = sql"""SELECT
         |    coupons.coupon_id, bets.duel_id, bets.winner, bets.rate, duels.duel_id, duels.home_team, duels.away_team, duels.result
         |FROM
         |    coupons NATURAL JOIN bets NATURAL JOIN duels
         |WHERE
         |    coupons.user_id = $userId""".stripMargin.query[(CouponId, BetRow, DuelRow)]

    for {
      ctx <- Ctx.ask
      _ <- L.debug(ctx.logMarker:_*)(query.sql)
      result <- query.stream.compile.toList.transact(DB)
    } yield result.groupByNel(_._1).mapValues(_.map(_.tail)).map {
      case (couponId, rows) =>
        CouponView.view(couponId, rows.toList)
    }.toList

  }

}


object CouponsService {

  case class CouponRow(coupon_id: CouponId, user_id: UserId)

  case class BetRow(duel_id: DuelId, winner: Option[TeamId], rate: Double) {
    def bid: Bet = Bet(duel_id, winner, rate)
  }



}