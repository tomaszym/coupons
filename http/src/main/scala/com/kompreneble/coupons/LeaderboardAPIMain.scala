package com.kompreneble.coupons

import cats.data.Kleisli
import cats.effect._
import doobie.Transactor
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import cats.mtl.implicits._
import cats.implicits._
import org.http4s.dsl.io._
import com.kompreneble.coupons.http.AppRoutes
import com.kompreneble.coupons.model.DuelResult
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.GZip
import org.http4s.server.blaze._
import org.http4s.implicits._
import org.http4s.server._
import org.http4s.server.Router

object LeaderboardAPIMain extends IOApp {

  type K[A] = Kleisli[IO, RequestContext, A]

  def run(args: List[String]): IO[ExitCode] = {

    println(DuelResult.withName("home_team_won"))

    for {
      config <- AppConfig.load

      server = {
        implicit val loggerInstance = Slf4jLogger.unsafeCreate[K]
        implicit val xa = Transactor.fromDriverManager[K](
          "org.postgresql.Driver",
          s"jdbc:postgresql:${config.postgres.database}",
          config.postgres.username,
          config.postgres.password
        )

        val rates = new RatesService[K]()
        val duels = new DuelsService[K](rates)
        val coupons = new CouponsService[K](
          duels,
          rates,
        )

        val appRoutes = new AppRoutes[IO](coupons, duels)

        val router = Router("/" -> GZip(appRoutes.couponsRoutes)).orNotFound
        val serverBuilder = BlazeServerBuilder[IO].bindHttp(config.http.port, config.http.host).withHttpApp(router)

        serverBuilder.resource
      }

      exitCode <- server.use(_ => IO.never).map { _ => ExitCode.Success }

    } yield exitCode
  }
}
