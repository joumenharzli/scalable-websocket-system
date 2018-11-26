package handler

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
    val wsUser: ActorRef = system.actorOf(SessionActor.props())

    val sink = Flow[Message]
      .to(Sink.actorRef(wsUser, CloseSession))

    val source = Source
      .actorRef(bufferSize = 10, overflowStrategy = OverflowStrategy.dropBuffer)
      .mapMaterializedValue { wsHandle =>
        wsUser ! CreateSession(wsHandle, userId)
        NotUsed
      }

    Flow.fromSinkAndSource(sink, source)

  }

}
