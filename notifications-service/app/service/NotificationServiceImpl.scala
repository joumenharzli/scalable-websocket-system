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

import cats.data.{NonEmptyList, Validated}
import com.datastax.driver.core.PagingState
import javax.inject.Singleton
import org.slf4j.{Logger, LoggerFactory}
import repository.NotificationRepository
import repository.support.Page
import service.dto.{NotificationToAddDto, UserNotificationDto}

import scala.concurrent.Future

@Singleton
class NotificationServiceImpl(val notificationRepository: NotificationRepository) extends NotificationService {

  val logger: Logger = LoggerFactory.getLogger(this.getClass.getName)

  override def insert(notification: NotificationToAddDto): Future[UserNotificationDto] = {

    logger.debug("Request to insert notification {}",notification)

    // https://github.com/DanielaSfregola/data-validation/blob/master/src/main/scala/ValidationUsingValidatedNelAndErr.sc

    (nonNullEntity(notification) , nonNullEntity(notification)) .mapN(NotificationToAddDto)

  }

  override def updateToSeen(id: UUID): Future[Unit]  = {

    logger.debug("Request to set notification {} to seen",id)

  }

  override def findByUserId(userId: UUID, pagingState: Option[PagingState]): Future[Page[List[UserNotificationDto]]]  = {

    logger.debug("Request to find notifications for the user {} and paging state",userId,pagingState)

  }


  def nonNullEntity(entity:NotificationToAddDto): Validated[NonEmptyList[String], NotificationToAddDto] = {
    Option(entity) match {
      case Some(x) => Validated.validNel(entity)
      case None => Validated.invalidNel("Notification cannot be null")
    }
  }

}
