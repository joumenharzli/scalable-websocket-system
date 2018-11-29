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

package handler

import actor._
import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.event.slf4j.Logger
import akka.http.scaladsl.model.ws.Message
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}

/**
 * A handler for web sockets that creates an actor when the client connects and kill it when it disconnects
 *
 * @author jaharzli
 */
object WebSocketHandler {

  val logger = Logger(this.getClass.getName)

  def handle(userId: String)(implicit system: ActorSystem,
                             eventsConsumer: ActorRef): Flow[Message, Message, NotUsed] = {

    logger.debug("Request to create connection for the user {}", userId)

    // Create an actor for every WebSocket connection
    val wsUser: ActorRef = system.actorOf(SessionActor.props(eventsConsumer))

    // Sends the elements of the stream to the created actor and when the stream is completed send him a CloseSession message
    val sink = Sink.actorRef(wsUser, CloseSession)

    // The source is represented as an Actor and send the created referee to wsUser.
    // This source is the way to send messages to the client
    val source = Source
      .actorRef(bufferSize = 10, overflowStrategy = OverflowStrategy.dropBuffer)
      .mapMaterializedValue { wsHandle =>
        wsUser ! CreateSession(wsHandle, userId)
        NotUsed
      }

    Flow.fromSinkAndSource(sink, source)

  }

}
