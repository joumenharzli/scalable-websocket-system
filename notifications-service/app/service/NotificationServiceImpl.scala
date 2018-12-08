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

package service

import java.util.UUID

import com.datastax.driver.core.PagingState
import javax.inject.Inject
import domain.Notification
import org.joda.time.DateTime
import play.api.Logger
import repository.NotificationRepository
import service.dto.{NotificationToAddDto, UserNotificationDto, UserNotificationPaginatedResult}
import service.support.Validator._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class NotificationServiceImpl @Inject()(repository: NotificationRepository, ec: ExecutionContext)
    extends NotificationService {

  val logger = Logger(this.getClass)

  implicit val executionContext: ExecutionContext = ec

  override def insert(
    notification: NotificationToAddDto
  ): Either[Seq[ValidationError], Future[UserNotificationDto]] = {

    logger.debug(s"Request to insert notification $notification")

    require(notification != null, "Notification cannot be null")

    validateNotification(notification)
      .map(toEntity)
      .map(repository.save(_).map(toDto))

  }

  override def updateToSeen(id: String): Try[Future[Unit]] = {

    logger.debug(s"Request to set notification $id to seen")

    require(id != null, "id cannot be null")

    repository.updateToSeen(UUID.fromString(id))

  }

  override def findByUserId(
    userId: String,
    pagingString: Option[String]
  ): Future[UserNotificationPaginatedResult] = {

    logger.debug(s"Request to find notifications for the user $userId and paging state $pagingString")

    require(userId != null, "user id cannot be null")
    require(pagingString != null, "pagingString cannot be null")

    val next = for {
      s     <- pagingString
      state <- Try(PagingState.fromString(s)).toOption
    } yield state

    repository
      .findByUserId(UUID.fromString(userId), next)
      .map(page => UserNotificationPaginatedResult(page.results.map(toDto), page.next))

  }

  private def validateNotification(
    notification: NotificationToAddDto
  ): Either[Seq[ValidationError], NotificationToAddDto] = {

    lazy val errors = notBlank(notification.content, "content cannot be null/empty") ++
      notNull(notification.userId, "user id in cannot be null")

    Either.cond(errors.isEmpty, notification, errors)

  }

  private def toEntity(e: NotificationToAddDto): Notification =
    Notification(id = UUID.randomUUID(),
                 content = e.content,
                 seen = false,
                 userId = e.userId,
                 createdAt = DateTime.now())

  private def toDto(e: Notification) = UserNotificationDto(e.id, e.content, e.seen, e.createdAt)

}
