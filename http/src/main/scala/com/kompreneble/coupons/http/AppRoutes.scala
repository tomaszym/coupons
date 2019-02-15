package com.kompreneble.coupons.http

import java.util.UUID

import io.chrisdavenport.fuuid.doobie.implicits._
import io.chrisdavenport.fuuid.FUUID
import cats.data.{Kleisli, NonEmptyList}
import cats.effect._
import cats.syntax.all._
import com.kompreneble.coupons.view.CouponRequest
import com.kompreneble.coupons.{CouponsService, DuelsService, RequestContext, UserId}
import io.circe._
import io.circe.syntax._
import org.http4s._
import org.http4s.dsl._
import org.http4s.circe._
import org.http4s.headers.`WWW-Authenticate`
import org.http4s.implicits._
import org.http4s.util.CaseInsensitiveString
import org.http4s.circe.CirceEntityDecoder._

class AppRoutes[F[_]](
  coupons: CouponsService[Kleisli[F, RequestContext, ?]],
  duels: DuelsService[Kleisli[F, RequestContext, ?]],
)(implicit
  contextShift: ContextShift[F],
  timer: Timer[F],
  E: ConcurrentEffect[F],
) extends Http4sDsl[F]{
  import com.kompreneble.coupons.view.DuelView.encoder

  def authed[A](req: Request[F])(f: UserId => F[Response[F]]): F[Response[F]] = {
    val userIdMaybe: Option[UserId] =
      req.headers.get(CaseInsensitiveString("user-id"))
        .flatMap(header => FUUID.fromString(header.value).toOption.map(FUUID.Unsafe.toUUID))

    userIdMaybe match {
      case Some(value) => f(value)
      case None =>
        val challenge = `WWW-Authenticate`(NonEmptyList.one(Challenge("dummy", "dummy")))
        Unauthorized.apply(challenge, "specify user-id in header")
    }
  }

  val couponsRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case request @ GET -> Root / "coupons" => authed(request) { userId =>
      Ok {
        coupons.getOfUser(userId).map { coupons =>
          Json.obj(
            "coupons" -> Json.arr(
              coupons.map { coupon =>
                Json.obj(
                  "couponId" -> coupon.couponId.asJson,
                  "userId" -> coupon.userId.asJson,
                  "bids" -> Json.arr(
                    coupon.bids.map { bid =>
                      Json.obj(
                        "duelId" -> bid.duelId.asJson,
                        "rate" -> Json.fromDoubleOrNull(bid.rate),
                        "winner" -> bid.winner.asJson,
                      )
                    }.toList: _*
                  )
                )
              }: _*
            )
          )
        }.apply(RequestContext.fresh(UUID.randomUUID()))
      }
    }
    case request @ POST -> Root / "coupons" => authed(request) { userId =>
      for {
        req <- request.as[CouponRequest]
        id <- coupons.createCoupon(userId, req).apply(RequestContext.fresh(UUID.randomUUID()))
        response <- Created.apply(id.asJson)
      } yield response
    }

    case GET -> Root / "duels" => Ok {
      duels.view.map(arr => Json.obj("duels" -> arr.asJson))
        .apply(RequestContext.fresh(UUID.randomUUID()))
    }

    case POST -> Root / "duels" / "populate" => Created {
      duels.populate(RequestContext.fresh(UUID.randomUUID()))
    }

    case POST -> Root / "duels" / "simulate" => Created {
      duels.simulate(RequestContext.fresh(UUID.randomUUID()))
    }

  }


}
