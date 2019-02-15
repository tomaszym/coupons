package com.kompreneble.coupons

class ServiceError(
  val code: Int,
  val message: String,
  val info: Map[String, String] = Map.empty,
  val userFacing: Boolean = false,
) extends Exception
