package com.kompreneble.coupons.http

import java.util.UUID

import io.chrisdavenport.fuuid.doobie.implicits._
import io.chrisdavenport.fuuid.FUUID
import cats.data.{Kleisli, NonEmptyList}
import cats.effect._
import cats.syntax.all._
import com.kompreneble.coupons.view.CouponRequest
import com.kompreneble.coupons._
import io.circe._
import io.circe.syntax._
import org.http4s._
import org.http4s.dsl._
import org.http4s.circe._
import org.http4s.headers.`WWW-Authenticate`
import org.http4s.implicits._
import org.http4s.util.CaseInsensitiveString
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.dsl.impl.EntityResponseGenerator

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
        coupons.view(userId).map { coupons =>

          coupons.asJson

        }.apply(RequestContext.fresh(UUID.randomUUID()))
      }.recoverWith {
        case error: ServiceError =>
          new EntityResponseGenerator[F] {
            override def status: Status = Status(error.code)
          } apply error.json
      }
    }
    case request @ POST -> Root / "coupons" => authed(request) { userId =>
      for {
        req <- request.as[CouponRequest]
        id <- coupons.createCoupon(userId, req).apply(RequestContext.fresh(UUID.randomUUID()))
        response <- Created.apply(id.asJson)
      } yield response
    }.recoverWith {
      case error: ServiceError =>
        new EntityResponseGenerator[F] {
          override def status: Status = Status(error.code)
        } apply error.json
    }

    case GET -> Root / "duels" => Ok {
      duels.view.map(arr => Json.obj("duels" -> arr.asJson))
        .apply(RequestContext.fresh(UUID.randomUUID()))
    }.recoverWith {
      case error: ServiceError =>
        new EntityResponseGenerator[F] {
          override def status: Status = Status(error.code)
        } apply error.json
    }

    case POST -> Root / "duels" / "populate" => Created {
      duels.populate(RequestContext.fresh(UUID.randomUUID()))
    }.recoverWith {
      case error: ServiceError =>
        new EntityResponseGenerator[F] {
          override def status: Status = Status(error.code)
        } apply error.json
    }

    case POST -> Root / "duels" / "simulate" => Created {
      duels.simulate(RequestContext.fresh(UUID.randomUUID()))
    }.recoverWith {
      case error: ServiceError =>
        new EntityResponseGenerator[F] {
          override def status: Status = Status(error.code)
        } apply error.json
    }

  }


}
