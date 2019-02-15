package com.kompreneble.coupons

import java.time.LocalDateTime
import java.util.UUID

import org.http4s.Headers

case class RequestContext(
  requestId: UUID,
  userId: UserId,
  clock: LocalDateTime,
  headers: Headers
) {
  def logMarker: List[(String, String)] = List(
    "request-id" -> requestId.toString
  )
}

object RequestContext {

  def fresh(userId: UserId): RequestContext = RequestContext(
    requestId = UUID.randomUUID(),
    userId = userId,
    clock = LocalDateTime.now(),
    headers = Headers.empty,
  )

}

