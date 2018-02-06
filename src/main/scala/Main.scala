import java.time.Instant

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorRef, ActorSystem, OneForOneStrategy, Props, Terminated}

import scala.concurrent.ExecutionContext
import scala.util.Success
import scala.concurrent.duration._

object Main extends App {

 import IC._

  val system = ActorSystem("BadSystem")
  implicit val ec : ExecutionContext = system.dispatcher

  val props = Props(new LoggingRouterActor()(ec))
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


class LoggingRouterActor(implicit ec:ExecutionContext) extends Actor {

  var router: Vector[ActorRef] = Vector.fill(3){
    val r = context.actorOf(Props[EventEnricher])
    println("creating initial actor")
    context watch r
    r
  }

  var i = 0

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 0, withinTimeRange = 10 minute) {
      case _: ArithmeticException      ⇒ Stop
      case _: NullPointerException     ⇒ Stop
      case _: IllegalArgumentException ⇒ Stop
      case _: Exception                ⇒ Stop
    }

  override def receive : Receive = {
    case e: Event => {
      val ref = router(i % router.length)
      i = i + 1
      ref ! e
    }
    case Terminated(a) =>
      println("Creating a new actor")
      router = router.filter(_ != a)
      val r = context.actorOf(Props[EventEnricher])
      context watch r
      router = router :+ r
  }
}

class EventEnricher extends Actor {
  val creationTime = Instant.now().toEpochMilli % 3
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
    case e: Event if creationTime != 1 =>  println(s"${context.self.path.name} - ${e.time} : SITE_ID ${sites(e.siteId)} - Log of ${e.eventType} type and value is ${e.value}")
    case _ => throw new IllegalArgumentException()
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

