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

