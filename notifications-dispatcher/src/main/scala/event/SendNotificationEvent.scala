package event

/**
  * A model for representing the notification
  *
  * @param id      id of the notification
  * @param content content of the notification
  * @param userId  id of the user
  */
case class SendNotificationEvent(id: String, content: String, userId: String)
