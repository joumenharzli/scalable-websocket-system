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

import Application.system
import actor.EventsConsumerActor
import akka.actor.{ActorRef, ActorSystem, Props, Terminated}
import akka.event.slf4j.Logger
import akka.stream.ActorMaterializer
import cakesolutions.kafka.{KafkaConsumer, KafkaProducer}
import cakesolutions.kafka.akka.{KafkaConsumerActor, KafkaProducerActor, ProducerRecords}
import event.SendNotificationEvent
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import server.WebServer

import event.SendNotificationEvent
import event.SendNotificationEventJsonSupport._
import spray.json._

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

  implicit val system: ActorSystem                        = ActorSystem("notification-dispatcher")
  implicit val materializer: ActorMaterializer            = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val eventsConsumer: ActorRef                   = startEventsConsumer()

  WebServer
    .start()
    .onComplete {
      case Success(serverBinding) =>
        logger.info("listening to: {}", serverBinding.localAddress)
      case Failure(ex) =>
        logger.error("Failed to start server, shutting down actor system. Exception is: {}", ex)
        system.terminate()
    }

  // attach shutdown handler to catch terminating signals as well as normal termination
  Runtime.getRuntime.addShutdownHook(new Thread("notification-dispatcher-shutdown-hook") {
    override def run(): Unit =
      Await.result(stopWebServerAndActorSystem(), 10.seconds)
  })

  /**
   * Start the kafka consumer and subscribe to topic
   *
   * @return a reference to the consumer actor
   */
  def startEventsConsumer(): ActorRef =
    system.actorOf(EventsConsumerActor.props())

  /**
   * Stop server and Actor System
   */
  def stopWebServerAndActorSystem(): Future[Terminated] =
    WebServer
      .stop()
      .flatMap(_ => system.terminate())

}


object ProducerKafka extends App {

  implicit val system: ActorSystem                        = ActorSystem("notification-dispatcher")
  implicit val materializer: ActorMaterializer            = ActorMaterializer()

  private val kafka: ActorRef = system.actorOf(
    KafkaProducerActor.props(KafkaProducer.Conf(Map("bootstrap.servers" -> "localhost:29092"),
    new StringSerializer, new StringSerializer)))

  (0 to 2000)
      .foreach(i=>{
        kafka ! ProducerRecords.fromKeyValues[String, String]("notifications",
          keyValues = Seq((Some("notifications"), SendNotificationEvent("1",s"message => $i","3").toJson.toString())),
          successResponse = Some("success"),
          failureResponse = Some("failure"))
      })





}