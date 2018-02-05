import akka.actor.ActorRef

object IC {
  implicit class OMG(ar: ActorRef) {
    def log(msg: Any): Unit = {
      ar ! msg
    }
  }
}
