package actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.ws.TextMessage
import akka.stream.scaladsl.Source
import model.NotificationJsonSupport._
import spray.json._


object SessionActor {
  def props(eventsConsumer: ActorRef): Props = Props(new SessionActor(eventsConsumer))
}

/**
  * This actor is created when a web socket connection is started it.
  * When a message [[SendToClient]] is received it checks if the client is concerned then it send it a notification
  *
  * @author jaharzli
  */
class SessionActor(val eventsConsumer: ActorRef) extends Actor with ActorLogging {

  private var client: Option[(ActorRef, String)] = None

  override def receive: Receive = {

    case CreateSession(ref, id) =>
      log.info("New client connected associated to the user {}", id)
      client = Some(ref, id)
      eventsConsumer ! SessionCreated(self, id)

    case SendToClient(notification, id) =>
      log.debug("Request to send notification {} to user {}", notification, id)

      client match {
        case Some((ref, userId)) if id == userId =>
          ref ! TextMessage(Source.single(notification.toJson.toString()))
          log.debug("Notification {} sent to user {}", notification, userId)
        case Some((ref, userId)) if id != userId =>
          // This should never happen
          log.warning("Notification {} is not allowed to be sent to user {}", notification, userId)
        case _ => // ignore
      }

    case CloseSession =>

      client match {
        case Some((ref, userId)) =>
          log.info("Client for the user {} disconnected", userId)
          eventsConsumer ! SessionClosed(self, userId)
        case None => log.info("No client is associated to this actor")
      }

      context.stop(self)
  }

}
