package model

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

/**
  * A model for representing the notification
  *
  * @param id      id of the notification
  * @param content content of the notification
  */
case class Notification(id: String, content: String)

/**
  * Support reading and writing json
  */
object NotificationJsonSupport extends DefaultJsonProtocol {
  implicit val notificationFormat: RootJsonFormat[Notification] = jsonFormat2(Notification)
}