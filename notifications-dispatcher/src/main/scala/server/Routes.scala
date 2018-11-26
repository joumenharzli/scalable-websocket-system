package server

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.{handleWebSocketMessages, parameters, path, _}
import akka.http.scaladsl.server.Route
import handler.WebSocketHandler

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
