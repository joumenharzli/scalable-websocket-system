package handler

import java.util.concurrent.TimeUnit

import actor.{ClientConnected, ClientDisconnected, WebSocketActor}
import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.event.slf4j.Logger
import akka.http.scaladsl.model.ws.Message
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

/**
  * A handler for web sockets that creates an actor when the client connects and kill it when it disconnects
  *
  * @author jaharzli
  */
object WebSocketHandler {

  val logger = Logger(this.getClass.getName)

  def handle(userId: String)(implicit system: ActorSystem): Flow[Message, Message, NotUsed] = {

    logger.debug(s"Request to create connection for the user $userId")

    // Create an actor for every WebSocket connection, this will represent the contact point to reach the user
    val wsUser: ActorRef = system.actorOf(WebSocketActor.props())

    val sink = Flow[Message]
      .to(Sink.actorRef(wsUser, ClientDisconnected))

    val source = Source
      .actorRef(bufferSize = 10, overflowStrategy = OverflowStrategy.dropBuffer)
      .mapMaterializedValue { wsHandle =>
        wsUser ! ClientConnected(wsHandle, userId)
        NotUsed
      }

    Flow.fromSinkAndSource(sink, source)

  }

  def getUser(userId: String)(implicit system: ActorSystem, executor: ExecutionContext) = {

    implicit val timeout: Timeout = Timeout(FiniteDuration(1, TimeUnit.SECONDS))

    system.actorSelection("user/" + userId).resolveOne().onComplete {
      case Success(actorRef) => actorRef
      case Failure(ex) => system.actorOf(UserActor.props(userId))
    }

  }
}
