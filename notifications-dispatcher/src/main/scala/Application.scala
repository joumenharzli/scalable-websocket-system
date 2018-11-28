import actor.EventsConsumerActor
import akka.actor.{ActorRef, ActorSystem, Terminated}
import akka.event.slf4j.Logger
import akka.stream.ActorMaterializer
import server.WebServer

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

/**
  * Init Actor System and materializer then launch the server
  * and wait for the shutdown to clean resources
  *
  * @author jaharzli
  */
object Application extends App {

  val logger = Logger(this.getClass.getName)

  implicit val system: ActorSystem = ActorSystem("notification-dispatcher")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val eventsConsumer: ActorRef = startEventsConsumer()

  WebServer
    .start()
    .onComplete {
      case Success(serverBinding) =>
        logger.info("listening to: {}", serverBinding.localAddress)
      case Failure(ex) =>
        logger.error(
          "Failed to start server, shutting down actor system. Exception is: {}", ex)
        system.terminate()
    }

  // attach shutdown handler to catch terminating signals as well as normal termination
  Runtime.getRuntime.addShutdownHook(
    new Thread("notification-dispatcher-shutdown-hook") {
      override def run(): Unit =
        Await.result(stopWebServerAndActorSystem(), 10.seconds)
    })

  /**
    * Start the kafka consumer and subscribe to topic
    *
    * @return a reference to the consumer actor
    */
  def startEventsConsumer(): ActorRef = {
    system.actorOf(EventsConsumerActor.props())
  }

  /**
    * Stop server and Actor System
    */
  def stopWebServerAndActorSystem(): Future[Terminated] = {
    WebServer
      .stop()
      .flatMap(_ => system.terminate())
  }

}
