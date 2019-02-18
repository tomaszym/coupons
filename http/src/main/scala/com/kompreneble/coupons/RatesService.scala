package com.kompreneble.coupons

import com.kompreneble.coupons.model.DuelRates
import cats.effect.Concurrent
import cats.mtl.ApplicativeAsk
import io.chrisdavenport.log4cats.Logger

import scala.util.Random

class RatesService[F[_]]()(implicit
  L: Logger[F],
  E: Concurrent[F],
  Ctx: ApplicativeAsk[F, RequestContext],
) {

  def rates(duels: Iterable[DuelId]): F[Map[DuelId, DuelRates]] = E.delay {
    duels.map { duel => duel -> {
      val r = new Random(duel.hashCode())
      def randomRate = ((r.nextDouble() * 10).toInt.toDouble / 10) + r.nextInt(5) +1
      val home = randomRate
      val tie = randomRate
      val away = randomRate

      DuelRates(
        duel,
        home,
        tie,
        away,
      )
    }}.toMap
  }

}
