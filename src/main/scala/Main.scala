import java.time.Instant

import akka.actor.{Actor, ActorSystem, Props}
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object Main extends App {

 import IC._

  val system = ActorSystem("BadSystem")
  implicit val ec : ExecutionContext = system.dispatcher

  val props = Props(new LoggingActor()(ec))
  val loggingActor = system.actorOf(props)

  val str = Stream.continually({Thread.sleep((Math.random() * 1000).toLong); EventGenerator.generate()})
    .take(20)
    .foreach(loggingActor log _)

  system.terminate() onComplete {
    case Success(any) => println("Terminated")
    case _ => println("Not Terminated")
  }

}

class Event(val eventType: String, val siteId: Int, val value: Double, val time: Instant)


class LoggingActor(implicit ec:ExecutionContext) extends Actor {
  import IC._
  implicit val timeout = Timeout.durationToTimeout(5 seconds)

  val myChild = context.actorOf(Props[EventEnricher])

  override def receive: Receive = {
    case e: Event => {
        myChild heyWhatsMySite e onComplete {
        case Success(site) =>     println(s"${e.time} : SITE_ID ${site} - Log of ${e.eventType} type and value is ${e.value}")
        case Failure(f) => println(s"Something bad has happened: $f")
    }
  }
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
    case e: Event => sender ! sites(e.siteId)
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

