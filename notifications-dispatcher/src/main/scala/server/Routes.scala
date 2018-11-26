package server

/**
  * Web Server Routes
  *
  * @author jaharzli
  */
object Routes {

  def routes(implicit system: ActorSystem): Route =
    path("ws") {
      parameters('id.as[String]) {
        id => {
          handleWebSocketMessages(WebSocketHandler.handle(id))
        }
      }

    }

}
