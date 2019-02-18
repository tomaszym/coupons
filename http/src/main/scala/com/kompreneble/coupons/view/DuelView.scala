package com.kompreneble.coupons.view

import cats.MonadError
import cats.syntax.all._
import com.kompreneble.coupons.errors.DuelIdMismatch
import com.kompreneble.coupons.model.{Duel, DuelRates}
import com.kompreneble.coupons.{DuelId, TeamId}
import io.circe.Encoder
import io.circe.derivation._

case class DuelView(
  duelId: DuelId,
  winnerRates: Map[TeamId, Double],
  tieRate: Double,
)

object DuelView {
  def view[F[_]](
    duel: Duel,
    rates: DuelRates,
  )(implicit
    E: MonadError[F,Throwable]
  ): F[DuelView] = for {
      _ <- if(duel.duelId == duel.duelId) E.unit else E.raiseError(DuelIdMismatch)
    } yield DuelView(
      duelId = duel.duelId,
      winnerRates = Map(
        duel.home -> rates.homeWins,
        duel.away -> rates.awayWins,
      ),
      tieRate = rates.tie,
    )

  implicit val encoder: Encoder[DuelView] = deriveEncoder

}