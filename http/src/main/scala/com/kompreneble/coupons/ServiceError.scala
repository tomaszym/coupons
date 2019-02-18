package com.kompreneble.coupons

import io.circe.Json

class ServiceError(
  val code: Int,
  val message: String,
  val info: Map[String, String] = Map.empty,
  val userFacing: Boolean = false,
) extends Exception {
  def json: Json = Json.fromString(message)
}
