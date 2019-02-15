package com.kompreneble.coupons

import cats.effect.IO
import com.kompreneble.coupons.AppConfig.{CouponsConfig, HttpConfig, PostgresConfig}
import pureconfig.ConvertHelpers.catchReadError
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect._

case class AppConfig(
  http: HttpConfig,
  postgres: PostgresConfig,
  coupons: CouponsConfig,
)
object AppConfig {

  case class HttpConfig(
    host: String,
    port: Int,
  )
  case class PostgresConfig(
    username: String,
    password: String,
    database: String,
    host: String,
    port: Int,
  )

  case class CouponsConfig(

  )


  val load: IO[AppConfig] = loadConfigF[IO, AppConfig]("app")
}

