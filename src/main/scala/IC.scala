import akka.actor.ActorRef

import scala.concurrent.Future
import akka.pattern.ask
import akka.util.Timeout

object IC {
  implicit class OMG(ar: ActorRef) {
    def log(msg: Any): Unit = {
      ar ! msg
    }

    def heyWhatsMySite(msg: Any)(implicit timeout: Timeout): Future[Any] = {
      ar ? msg
    }
  }
}
