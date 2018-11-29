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

package server

import akka.Done
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import server.Routes.routes

import scala.concurrent.{ExecutionContextExecutor, Future}

/**
 * Http Web Server
 *
 * @author jaharzli
 */
object WebServer {

  // Load configuration
  private val config: Config                       = ConfigFactory.load().getConfig("server")
  private val interface: String                    = config.getString("interface")
  private val port: Int                            = config.getInt("port")
  private var instance: Future[Http.ServerBinding] = _

  /**
   * Start server
   *
   * @return a future to the server
   */
  def start()(implicit system: ActorSystem,
              materializer: ActorMaterializer,
              eventsConsumer: ActorRef): Future[Http.ServerBinding] = {

    val httpHandle = Http().bindAndHandle(routes, interface, port)
    instance = httpHandle
    httpHandle

  }

  /**
   * Stop server
   */
  def stop()(implicit executionContext: ExecutionContextExecutor): Future[Done] =
    instance.flatMap(_.unbind)

}
