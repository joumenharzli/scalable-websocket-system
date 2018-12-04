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

import cats.data.Validated.{Invalid, Valid}
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Writes}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import play.libs.Json
import service.NotificationService
import service.dto.NotificationToAddDto

import scala.concurrent.Future

/**
  * REST Controller for notifications
  *
  * @author jaharzli
  */
@Singleton
class NotificationController @Inject()(cc: ControllerComponents, service: NotificationService) extends AbstractController(cc) {

  def add: Action[JsValue] = Action.async(parse.json) { request => {
    val payload = request.body.as[NotificationToAddDto]
    service.insert(payload) match {
      case Valid(x) => Future.successful(Ok(x.toJson))
      case Invalid(x) => Future.successful(BadRequest(x.toJson))
    }
  }
  }

  def updateToSeen(id: String): Action[JsValue] = Action.async(parse.json) { request => {
    service.updateToSeen(id) match {
      case Valid(x) => Future.successful(NoContent)
      case Invalid(x) => Future.successful(BadRequest(x.toJson))
    }
  }
  }

  def findByUserId(userId: String,
                   paging: Option[String]): Action[AnyContent] = Action.async(
    service.findByUserId(userId, paging) match {
      case Valid(x) => Future.successful(Ok(x.toJson))
      case Invalid(x) => Future.successful(BadRequest(x.toJson))
    }
  )

  implicit class Jsonable[A](a: A) {
    def toJson(implicit writes: Writes[A]): JsValue = Json.toJson(a)(writes)
  }

}
