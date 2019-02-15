package com.kompreneble.coupons

import cats.syntax.all._
import cats.instances.vector._
import com.kompreneble.coupons.model._
import cats.effect.Concurrent
import cats.mtl.ApplicativeAsk
import com.kompreneble.coupons.view.DuelView
import io.chrisdavenport.log4cats.StructuredLogger
import com.kompreneble.coupons.DuelsService._
import com.kompreneble.coupons.errors.MissingRates
import doobie.Transactor
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.implicits._
import doobie.util.Meta

class DuelsService[F[_]](
  ratesService: RatesService[F],
)(implicit
  L: StructuredLogger[F],
  E: Concurrent[F],
  Ctx: ApplicativeAsk[F, RequestContext],
  DB: Transactor[F],
) {

  private def getDuels: F[Map[DuelId, Duel]] =
  {

    val query = sql"""SELECT
                         duel_id, home_team, away_team, result
                     FROM
                         duels""".query[DuelRow]

    for {
      ctx <- Ctx.ask
      _ <- L.debug(ctx.logMarker:_*)(query.sql)
      result <- query.stream.compile.toVector.transact(DB)
    } yield result.map { row =>
        row.duel_id -> Duel(row.duel_id, row.home_team, row.away_team, row.result)
    }.toMap
  }

  def view: F[Vector[DuelView]] = {
    for {
      duels <- getDuels
      rates <- ratesService.rates(duels.keys)
      views <- duels.values.map { duel =>
        rates.get(duel.duelId) match {
          case Some(value) => DuelView.view(duel, value)(E)
          case None => E.raiseError[DuelView](MissingRates)
        }
      }.toVector.sequence[F, DuelView]
    } yield views
  }

  def simulate: F[Unit] = {

    val query =
      sql"""
           update duels
           set result=(enum_range(NULL::duel_result))[floor(random()*3)+1]
         """.update

    for {
      ctx <- Ctx.ask
      _ <- L.debug(ctx.logMarker:_*)(query.sql)
      _ <- query.run.transact(DB)
    } yield ()
  }


  def populate: F[Unit] = {

    val query =
      sql"""insert into duels (duel_id, home_team, away_team) values (uuid_generate_v4(),uuid_generate_v4(),uuid_generate_v4())
         """.update

    for {
      ctx <- Ctx.ask
      _ <- L.debug(ctx.logMarker:_*)(query.sql)
      _ <- query.run.transact(DB)
    } yield ()
  }
}

object DuelsService {

  implicit val DuelResultMeta: Meta[DuelResult] =
    pgEnumStringOpt("result", DuelResult.withNameOption, _.entryName)

  case class DuelRow(
    duel_id: DuelId,
    home_team: TeamId,
    away_team: TeamId,
    result: Option[DuelResult],
  )
}
