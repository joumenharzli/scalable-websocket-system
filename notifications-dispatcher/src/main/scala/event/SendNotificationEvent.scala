package event

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

/**
  * A model for representing the notification
  *
  * @param id      id of the notification
  * @param content content of the notification
  * @param userId  id of the user
  */
case class SendNotificationEvent(id: String, content: String, userId: String)

/**
  * Support reading and writing json
  */
object SendNotificationEventJsonSupport extends DefaultJsonProtocol {
  implicit val sendNotificationEventFormat: RootJsonFormat[SendNotificationEvent] = jsonFormat3(SendNotificationEvent)
}