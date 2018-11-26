package actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.ws.TextMessage
import akka.stream.scaladsl.Source


object WebSocketActor {
  def props(): Props = Props(new WebSocketActor())
}

/**
  * This actor is created when a web socket connection is started it.
  * When a message [[SendToClient]] is received it checks if the client is concerned then it send it a notification
  *
  * @author jaharzli
  */
class WebSocketActor extends Actor with ActorLogging {

  private var client: Option[(ActorRef, String)] = None

  override def receive: Receive = {

    case ClientConnected(ref, id) =>
      log.info(s"New client connected associated to the user $id")
      client = Some(ref, id)

    case SendToClient(notification, id) =>
      log.debug(s"Request to send notification $notification to user $id")

      client match {
        case Some((ref, userId)) if id == userId =>
          ref ! TextMessage(Source.single(""))
          log.debug(s"Notification $notification sent to user $userId")
      }

    case ClientDisconnected =>

      client match {
        case Some((ref, userId)) => log.info(s"Client for the user $userId disconnected")
        case None => log.info("No client is associated to this actor")
      }

      context.stop(self)
  }

}
