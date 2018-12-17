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

import java.util.UUID

import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import service.NotificationService
import service.dto.NotificationToAddDto
import service.support.Validator

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
 * REST Controller for notifications
 *
 * @author jaharzli
 */
@Singleton
class NotificationController @Inject()(cc: ControllerComponents, service: NotificationService)(
  implicit ec: ExecutionContext
) extends AbstractController(cc) {

  val logger = Logger(this.getClass)

  def add: Action[JsValue] = Action.async(parse.json) { implicit request =>
    {
      val payload = request.body.as[NotificationToAddDto]

      logger.debug(s"REST request to insert notification $payload")

      service.insert(payload) match {
        case Right(future) => future.map(Json.toJson(_)).map(Ok(_))
        case Left(errors)  => handleErrors(errors)
      }

    }
  }

  def updateToSeen(userId: String, createdAt: String, notificationId: String): Action[JsValue] =
    Action.async(parse.json) { implicit request =>
      {

        val userIdUuid         = UUID.fromString(userId)
        val dateTime           = DateTime.parse(createdAt)
        val notificationIdUuid = UUID.fromString(notificationId)

        logger.debug(s"REST request to set notification $notificationId to seen")

        service.updateToSeen(userIdUuid, dateTime, notificationIdUuid) match {
          case Success(_) => Future.successful(NoContent)
          case Failure(_) => Future.successful(NotFound)
        }

      }
    }

  def findByUserId(userId: String, paging: Option[String]): Action[AnyContent] = {

    val userIdUuid = UUID.fromString(userId)

    logger.debug(s"REST request to find notifications for the user $userId and paging state $paging")

    Action.async(
      service.findByUserId(userIdUuid, paging).map(Json.toJson(_)).map(Ok(_))
    )

  }

  private def handleErrors(errors: Seq[Validator.ValidationError]): Future[Result] =
    Future.successful(BadRequest(Json.toJson(errors.toList)))

}
