package server

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import server.Routes.routes

import scala.concurrent.{ExecutionContextExecutor, Future}

/**
  * Http Web Server
  *
  * @author jaharzli
  */
object WebServer {

  // Load configuration
  private val config: Config = ConfigFactory.load().getConfig("server")
  private val interface: String = config.getString("interface")
  private val port: Int = config.getInt("port")
  private var instance: Future[Http.ServerBinding] = _

  /**
    * Start server
    *
    * @return a future to the server
    */
  def start()(implicit system: ActorSystem,
              materializer: ActorMaterializer): Future[Http.ServerBinding] = {

    val httpHandle = Http().bindAndHandle(routes, interface, port)
    instance = httpHandle
    httpHandle

  }

  /**
    * Stop server
    */
  def stop()(
    implicit executionContext: ExecutionContextExecutor): Future[Done] = {
    instance.flatMap(_.unbind)
  }

}
