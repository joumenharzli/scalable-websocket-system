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

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import cakesolutions.kafka.KafkaConsumer
import cakesolutions.kafka.akka.KafkaConsumerActor.{Confirm, Subscribe}
import cakesolutions.kafka.akka.{ConsumerRecords, KafkaConsumerActor}
import com.typesafe.config.{Config, ConfigFactory}
import event.SendNotificationEvent
import event.SendNotificationEventJsonSupport._
import model.Notification
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.serialization.StringDeserializer
import event.SendNotificationEvent
import event.SendNotificationEventJsonSupport._
import spray.json._

import scala.collection.mutable
import scala.concurrent.duration._

object EventsConsumerActor {
  def props() = Props(new EventsConsumerActor())
}

/**
 * This actor consumes events from kafka then parse them them distribute them to [[SessionActor]]
 *
 * @author jaharzli
 */
class EventsConsumerActor extends Actor with ActorLogging {

  // Configurations
  private val config: Config   = ConfigFactory.load().getConfig("kafka-consumer")
  private val topic: String    = config.getString("topic")
  private val servers: String  = config.getString("servers")
  private val groupId: String  = config.getString("group-id")
  private val scheduleInterval = 1.seconds
  private val maxRedeliveries  = 3.seconds

  private val registry: mutable.Map[String, List[ActorRef]] = mutable.Map()
  private val recordsExtractor                              = ConsumerRecords.extractor[String, String]

  createConsumerAndSubscribeToTopic()

  override def receive: Receive = {
    case SessionCreated(actor, userId) => addUserSession(actor, userId)
    case SessionClosed(actor, userId)  => removeUserSession(actor, userId)
    case recordsExtractor(records)     => processRecords(records)
  }

  /**
   * Create the kafka consumer and subscribe to the provided topic
   */
  private def createConsumerAndSubscribeToTopic(): Unit = {

    log.debug("Request to create consumer and subscribe to topic {} in kafka {} with group id {}",
              topic,
              servers,
              groupId)

    val conf     = consumerConfiguration()
    val consumer = buildConsumer(conf)
    consumer ! Subscribe.AutoPartition(List(topic))

  }

  /**
   * Add user session to registry
   *
   * @param actor  reference if the actor
   * @param userId id of the user
   */
  private def addUserSession(actor: ActorRef, userId: String): Unit = {

    val actors: List[ActorRef] = registry.get(userId) match {
      case Some(list) => list
      case None       =>
        // init list
        val list = List()
        registry += (userId -> list)
        list
    }

    registry.update(userId, actor :: actors)

  }

  /**
   * Remove user session from registry
   *
   * @param actor  reference if the actor
   * @param userId id of the user
   */
  private def removeUserSession(actor: ActorRef, userId: String): Unit =
    registry.get(userId) match {
      case Some(list) => registry.update(userId, list.filter(el => el != actor))
      case None       => // ignore
    }

  /**
   * Process received records
   *
   * @param records records
   */
  private def processRecords(records: ConsumerRecords[String, String]): Unit = {

    records.pairs
      .map { case (_, data) => data }
      .foreach(data => {

        val event        = data.parseJson.convertTo[SendNotificationEvent]
        val userId       = event.userId
        val notification = Notification(event.id, event.content)

        registry.get(userId) match {
          case Some(list) =>
            list.foreach(actor => {
              actor ! SendToClient(notification, userId)
            })
          case None => // ignore
        }

      })

    sender() ! Confirm(records.offsets, commit = true)

  }

  /**
   * @return the configuration of the consumer
   */
  private def consumerConfiguration(): KafkaConsumer.Conf[String, String] =
    KafkaConsumer.Conf(
      new StringDeserializer,
      new StringDeserializer,
      groupId = groupId,
      enableAutoCommit = false,
      autoOffsetReset = OffsetResetStrategy.EARLIEST,
      bootstrapServers = servers
    )

  /**
   * Build a kafka consumer
   *
   * @param consumerConfig config of the conumer
   * @return reference to the consumer
   */
  private def buildConsumer(consumerConfig: KafkaConsumer.Conf[String, String]): ActorRef = {

    val actorConfig = KafkaConsumerActor.Conf(scheduleInterval, maxRedeliveries)
    context.actorOf(KafkaConsumerActor.props(consumerConfig, actorConfig, self))

  }

}
