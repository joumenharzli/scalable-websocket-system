/*
 * Copyright (C) 2018  Joumen Ali HARZLI
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package web

import cats.data.NonEmptyChain
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._
import javax.inject.{Inject, Singleton}
import play.api.libs.json._
import play.api.mvc._
import play.libs.Json
import service.NotificationService
import service.dto.NotificationToAddDto

import scala.concurrent.{ExecutionContext, Future}

/**
 * REST Controller for notifications
 *
 * @author jaharzli
 */
@Singleton
class NotificationController @Inject()(cc: ControllerComponents, service: NotificationService)(
  implicit ec: ExecutionContext
) extends AbstractController(cc) {

  def add: Action[JsValue] = Action.async(parse.json) { request =>
    {
      val payload = request.body.as[NotificationToAddDto]
      service.insert(payload) match {
        case Valid(future)   => future.map(Json.toJson).map(Ok(_))
        case Invalid(errors) => handleErrors(errors)
      }
    }
  }

  def updateToSeen(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    {
      service.updateToSeen(id) match {
        case Valid(_)        => Future.successful(NoContent)
        case Invalid(errors) => handleErrors(errors)
      }
    }
  }

  def findByUserId(userId: String, paging: Option[String]): Action[AnyContent] = Action.async(
    service.findByUserId(userId, paging) match {
      case Valid(future)   => future.map(Json.toJson).map(Ok(_))
      case Invalid(errors) => handleErrors(errors)
    }
  )

  private def handleErrors(errors: NonEmptyChain[String]): Future[Result] =
    Future.successful(BadRequest(Json.toJson(errors.toList)))

}
