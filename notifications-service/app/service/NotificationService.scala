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

import domain.Notification
import org.joda.time.DateTime
import service.dto.{NotificationToAddDto, UserNotificationDto, UserNotificationPaginatedResult}
import service.support.Validator.ValidationError

import scala.concurrent.Future
import scala.util.Try

/**
 * A service for managing [[Notification]]
 *
 * @author jaharzli
 */
trait NotificationService {

  /**
   * Insert a new notification
   *
   * @param notification entity to insert
   * @return the inserted entity or the validation errors
   * @throws IllegalArgumentException if any given argument is invalid
   */
  def insert(notification: NotificationToAddDto): Either[Seq[ValidationError], Future[UserNotificationDto]]

  /**
   * Update notification property seen to true
   *
   * @param userId id of the user
   * @param createdAt date of creation of the notification
   * @param notificationId id of the notification
   * @throws IllegalArgumentException if any given argument is invalid
   */
  def updateToSeen(userId: UUID, createdAt: DateTime, notificationId: UUID): Try[Future[Unit]]

  /**
   * Find notifications by user id
   *
   * @param userId      id of the user
   * @param pagingState state of the pagination this is blank for the first page
   * @return the found notifications and the next paging state
   * @throws IllegalArgumentException if any given argument is invalid
   */
  def findByUserId(
    userId: UUID,
    pagingState: Option[String]
  ): Future[UserNotificationPaginatedResult]

}
