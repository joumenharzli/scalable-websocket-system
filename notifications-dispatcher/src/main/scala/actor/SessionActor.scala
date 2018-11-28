package actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.ws.TextMessage
import akka.stream.scaladsl.Source


object SessionActor {
  def props(): Props = Props(new SessionActor())
}

/**
  * This actor is created when a web socket connection is started it.
  * When a message [[SendToClient]] is received it checks if the client is concerned then it send it a notification
  *
  * @author jaharzli
  */
class SessionActor extends Actor with ActorLogging {

  private var client: Option[(ActorRef, String)] = None

  override def receive: Receive = {

    case CreateSession(ref, id) =>
      log.info("New client connected associated to the user ", id)
      client = Some(ref, id)

    case SendToClient(notification, id) =>
      log.debug("Request to send notification {} to user {}", notification, id)

      client match {
        case Some((ref, userId)) if id == userId =>
          ref ! TextMessage(Source.single(s"Hey => ${notification.content}"))
          log.debug("Notification {} sent to user {}", notification, userId)
        case _ => // ignore
      }

    case CloseSession =>

      client match {
        case Some((ref, userId)) => log.info("Client for the user {} disconnected", userId)
        case None => log.info("No client is associated to this actor")
      }

      context.stop(self)
  }

}
