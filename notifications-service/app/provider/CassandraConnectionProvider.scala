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

package provider

import com.outworkers.phantom.connectors.{CassandraConnection, ContactPoints}
import com.typesafe.config.Config
import javax.inject.{Inject, Provider, Singleton}

import scala.collection.JavaConverters._

/**
  * A provider for [[CassandraConnection]]
  *
  * @author jaharzli
  */
@Singleton
class CassandraConnectionProvider @Inject()(config: Config) extends Provider[CassandraConnection] {

  override def get(): CassandraConnection = {

    val contactPoints = config.getStringList("cassandra.contactPoints")
    val keyspace = config.getString("cassandra.keyspace")

    ContactPoints(contactPoints.asScala)
      .keySpace(keyspace)

  }
}
