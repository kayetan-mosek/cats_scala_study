package effetsdemo

import cats.*
import cats.effect.*
import cats.effect.std.*
import cats.effect.implicits.*
import cats.implicits.*

import scala.concurrent.duration.*

object QueueApp extends IOApp:


  sealed trait Event

  case class UserAdded(id:Long) extends Event
  case class UserDeleted(id:Long) extends Event

  def producer(queue: Queue[IO,Event]): IO[Nothing] =
    val generateEvent: IO[Event] = IO {
      val id = (math.random() * 1000).toLong
      if (math.random() < 0.5) UserAdded(id)
      else UserDeleted(id)
    }
    (IO.sleep(100.millis) *> generateEvent.flatMap(queue.offer)).foreverM

  def consumer(queue: Queue[IO,Event]): IO[Nothing] =
    queue.take.flatMap(IO.println).foreverM

  override def run(args: List[String]): IO[ExitCode] =
    Queue.unbounded[IO, Event].flatMap { queue =>
      producer(queue).both(consumer(queue))
    }.timeoutTo(3.seconds, IO.unit)
      .as(ExitCode.Success)


  end run
end QueueApp

