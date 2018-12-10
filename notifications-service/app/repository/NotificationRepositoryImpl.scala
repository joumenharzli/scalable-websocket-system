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

package repository

import java.util.UUID

import com.datastax.driver.core.PagingState
import com.outworkers.phantom.connectors.CassandraConnection
import com.outworkers.phantom.dsl._
import com.typesafe.config.Config
import domain.Notification
import javax.inject.Inject
import repository.support.Page

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

/**
 * An implementation of [[NotificationRepository]]
 *
 * @author jaharzli
 */
class NotificationRepositoryImpl @Inject()(config: Config, connection: CassandraConnection, ec: ExecutionContext)
    extends Table[NotificationRepositoryImpl, Notification]
    with NotificationRepository {

  implicit override def space: KeySpace = connection.provider.space

  implicit override def session: Session = connection.session

  override val tableName: String = "notifications"

  implicit val executionContext: ExecutionContext = ec

  val pageSize: Int    = config.getInt("cassandra.notifications.page-size")
  val maxWaitTime: Int = config.getInt("cassandra.tables.create.maxWaitTime")

  object id extends UUIDColumn with PartitionKey

  object content extends StringColumn

  object seen extends BooleanColumn

  object userId extends UUIDColumn with Index {
    override def name: String = "user_id"
  }

  object createdAt extends DateTimeColumn {
    override def name: String = "created_at"
  }

  // create table if not exists
  Await.ready(this.create.ifNotExists().future(), maxWaitTime.seconds)

  override def save(notification: Notification): Future[Notification] = {

    logger.debug(s"DB request to insert notification $notification")

    require(notification != null, "Notification cannot be null")

    insert
      .value(_.id, notification.id)
      .value(_.content, notification.content)
      .value(_.seen, notification.seen)
      .value(_.userId, notification.userId)
      .value(_.createdAt, notification.createdAt)
      .consistencyLevel_=(ConsistencyLevel.LOCAL_QUORUM)
      .future()
      .map(_ => notification)
  }

  override def updateToSeen(id: UUID): Try[Future[Unit]] = {

    logger.debug(s"DB request to set notification $id to seen")

    require(id != null, "id cannot be null")

    Try(
      update
        .where(_.id eqs id)
        .modify(_.seen setTo true)
        .consistencyLevel_=(ConsistencyLevel.LOCAL_QUORUM)
        .future()
        .map(_ => ())
    )
  }

  override def findByUserId(userId: UUID, pagingState: Option[PagingState]): Future[Page[List[Notification]]] = {

    logger.debug(s"DB request to find notifications for the user $userId and paging state $pagingState")

    require(userId != null, "user id cannot be null")
    require(pagingState != null, "pagingString cannot be null")

    val stmt = select
      .where(_.userId eqs userId)

    // see https://docs.datastax.com/en/developer/java-driver/3.6/manual/paging/
    (pagingState match {
      case Some(state) => stmt.paginateRecord(state)
      case None        => stmt.paginateRecord(_.setFetchSize(pageSize))
    }).map(result => Page(result.records, Option(result.pagingState).map(_.toString)))

  }

}
