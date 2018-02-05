import java.time.Instant

import akka.actor.{Actor, ActorSystem, Props}

import scala.util.Success

object Main extends App {

 import IC._

 val system = ActorSystem("BadSystem")
 val props = Props[LoggingActor]
 val loggingActor = system.actorOf(props)
 implicit val ec = system.dispatcher


 val str = Stream.continually(
   {Thread.sleep((Math.random() * 1000).toLong); EventGenerator.generate()})
   .take(300)
   .foreach(loggingActor log _)


  system.terminate() onComplete {
    case Success(any) => println("Terminated")
    case _ => println("Not Terminated")
  }

}


class Event(val eventType: String, val siteId: Int, val value: Double, val time: Instant)


class LoggingActor extends Actor {
  val myChild = context.actorOf(Props[EventEnricher])
 override def receive: Receive = {
  case e: Event => myChild ! e
 }
}

class EventEnricher extends Actor {
  val sites = Map(
    0 -> "site number 0",
    1 -> "site number 1",
    2 -> "site number 2",
    3 -> "site number 3",
    4 -> "site number 4",
    5 -> "site number 5",
    6 -> "site number 6",
    7 -> "site number 7",
    8 -> "site number 8",
    9 -> "site number 9",
    10 -> "site number 10"
  )

  override def receive: Receive = {
    case e: Event => println(s"${e.time} : SITE_ID ${sites(e.siteId)} - Log of ${e.eventType} type and value is ${e.value}")
  }
}

object EventGenerator{
  def generate(): Event = {
    new Event(
      time = Instant.now(),
      eventType = "Log line",
      value = Math.random(),
      siteId = (Math.random() * 10).toInt
    )
  }
}

