package actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import cakesolutions.kafka.KafkaConsumer
import cakesolutions.kafka.akka.KafkaConsumerActor.{Confirm, Subscribe}
import cakesolutions.kafka.akka.{ConsumerRecords, KafkaConsumerActor}
import com.typesafe.config.{Config, ConfigFactory}
import model.Notification
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.serialization.StringDeserializer

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
  private val config: Config = ConfigFactory.load().getConfig("kafka-consumer")
  private val topic: String = config.getString("topic")
  private val servers: String = config.getString("servers")
  private val groupId: String = config.getString("group-id")
  private val scheduleInterval = 1.seconds
  private val maxRedeliveries = 3.seconds

  private val registry: mutable.Map[String, List[ActorRef]] = mutable.Map()
  private val recordsExtractor = ConsumerRecords.extractor[String, String]

  createConsumerAndSubscribeToTopic()

  override def receive: Receive = {
    case SessionCreated(actor, userId) => addUserSession(actor, userId)
    case SessionClosed(actor, userId) => removeUserSession(actor, userId)
    case recordsExtractor(records) => processRecords(records)
  }

  /**
    * Create the kafka consumer and subscribe to the provided topic
    */
  private def createConsumerAndSubscribeToTopic(): Unit = {

    log.debug(
      "Request to create consumer and subscribe to topic {} in kafka {} with group id {}", topic, servers, groupId)

    val conf = consumerConfiguration()
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
      case None =>
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
  private def removeUserSession(actor: ActorRef, userId: String): Unit = {

    registry.get(userId) match {
      case Some(list) => registry.update(userId, list.filter(el => el != actor))
      case None => // ignore
    }

  }

  /**
    * Process received records
    *
    * @param records records
    */
  private def processRecords(records: ConsumerRecords[String, String]): Unit = {

    records.pairs.map { case (_, data) => data }
      .foreach(data => {

        val strings = data.split("#")
        val userId = strings(0)
        val notificationId = strings(1)
        val notificationContent = strings(2)

        registry.get(userId) match {
          case Some(list) =>
            list.foreach(actor => actor ! SendToClient(Notification(notificationId, notificationContent), userId))
          case None => // ignore
        }

      })

    sender() ! Confirm(records.offsets, commit = true)

  }

  /**
    * @return the configuration of the consumer
    */
  private def consumerConfiguration(): KafkaConsumer.Conf[String, String] = {

    KafkaConsumer.Conf(
      new StringDeserializer,
      new StringDeserializer,
      groupId = groupId,
      enableAutoCommit = false,
      autoOffsetReset = OffsetResetStrategy.EARLIEST,
      bootstrapServers = servers
    )

  }

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
