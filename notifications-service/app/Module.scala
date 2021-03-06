import com.google.inject.AbstractModule
import com.outworkers.phantom.connectors.CassandraConnection
import javax.inject.Singleton
import net.codingwell.scalaguice.ScalaModule
import provider.CassandraConnectionProvider
import repository.{NotificationRepository, NotificationRepositoryImpl}
import service.{NotificationService, NotificationServiceImpl}

/**
 * Guice Module
 *
 * @author jaharzli
 */
class Module extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[CassandraConnection].toProvider[CassandraConnectionProvider].in[Singleton]
    bind[NotificationRepository].to[NotificationRepositoryImpl].in[Singleton]
    bind[NotificationService].to[NotificationServiceImpl].in[Singleton]
  }

}
