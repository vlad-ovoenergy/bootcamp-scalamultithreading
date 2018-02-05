import java.time.Instant

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object Main extends App {

 import IC._

 val system = ActorSystem("BadSystem")
 val props = Props[LoggingActor]
 val loggingActor = system.actorOf(props)

 val str = Stream.continually({Thread.sleep((Math.random() * 1000).toLong); EventGenerator.generate()}).take(300).foreach(msg => loggingActor log msg )

}

class Event(val eventType: String, val siteId: Int, val value: Double, val time: Instant)


class LoggingActor extends Actor {
 override def receive: Receive = {
  case e: Event => println(s"${e.time} : SITE_ID ${e.siteId} - Log of ${e.eventType} type and value is ${e.value}")
 }
}

object EventGenerator{
  def generate(): Event = {
    new Event(
      time = Instant.now(),
      eventType = "Log line",
      value = Math.random(),
      siteId = 100
    )
  }
}

