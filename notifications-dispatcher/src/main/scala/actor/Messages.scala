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

package actor

import akka.actor.ActorRef
import model.Notification

/**
 * Message sent when a new client connects
 *
 * @param actorRef a reference for the actor of the connected client
 * @param userId   id of the user
 */
case class CreateSession(actorRef: ActorRef, userId: String)

/**
 * Message sent when the client disconnect
 */
case class CloseSession()

/**
 * Message sent when the client disconnect
 *
 * @param notification notification to send
 * @param userId       id of the user
 */
case class SendToClient(notification: Notification, userId: String)

/**
 * Message sent when a session is created
 *
 * @param actorRef a reference for the session
 * @param userId   id of the user
 */
case class SessionCreated(actorRef: ActorRef, userId: String)

/**
 *
 * Message sent when a session is closed
 *
 * @param actorRef a reference for the session
 * @param userId   id of the user
 */
case class SessionClosed(actorRef: ActorRef, userId: String)
