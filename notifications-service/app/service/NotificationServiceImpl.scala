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

import cats.data._
import cats.implicits._
import com.datastax.driver.core.PagingState
import domain.Notification
import javax.inject.Singleton
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}
import repository.NotificationRepository
import repository.support.Page
import service.dto.NotificationToAddDto
import service.support.Validator._

import scala.concurrent.Future
import scala.util.Try

@Singleton
class NotificationServiceImpl(val repository: NotificationRepository) extends NotificationService {

  val logger: Logger = LoggerFactory.getLogger(this.getClass.getName)

  override def insert(notification: NotificationToAddDto): Validated[NonEmptyChain[String], Future[Notification]] = {

    logger.debug("Request to insert notification {}", notification)

    validateNotification(notification)
      .map(toEntity)
      .map(repository.save)

  }

  override def updateToSeen(id: UUID): Validated[NonEmptyChain[String], Try[Future[Unit]]] = {

    logger.debug("Request to set notification {} to seen", id)

    notNull(id, "id in cannot be null")
      .map(repository.updateToSeen)

  }

  override def findByUserId(
                             userId: UUID,
                             pagingString: Option[String]
                           ): Validated[NonEmptyChain[String], Future[Page[List[Notification]]]] = {

    logger.debug("Request to find notifications for the user {} and paging state", userId, pagingString)

    val next = for {
      s <- pagingString
      state <- Try(PagingState.fromString(s)).toOption
    } yield state

    notNull(userId, "user id in cannot be null")
      .map(id => repository.findByUserId(id, next))

  }

  private def validateNotification(
                                    notification: NotificationToAddDto
                                  ): Validated[NonEmptyChain[String], NotificationToAddDto] =
    notNull(notification, "notification cannot be null")
      .andThen(
        e =>
          (
            notBlank(e.content, "content cannot be null/empty"),
            notNull(e.userId, "user id in cannot be null")
          ).mapN(NotificationToAddDto)
      )

  private def toEntity(e: NotificationToAddDto): Notification =
    Notification(id = UUID.randomUUID(),
      content = e.content,
      seen = false,
      userId = e.userId,
      createdAt = DateTime.now())

}
