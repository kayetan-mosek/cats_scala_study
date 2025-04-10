package effetsdemo

import cats.*
import cats.effect.*

import java.util.concurrent.Executors
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

object AsyncApp extends IOApp:

  case class Person(id: String, name: String)

  def findPersonById(id: String)(implicit ec: ExecutionContext): Future[Person] = Future {
    println(s"${Thread.currentThread().getName}")
    Person(id, "Random name")
  }

  def findByIdIO(id: String): IO[Person] = {
    implicit val ec: ExecutionContext = ExecutionContext.global
    IO(Await.result(findPersonById(id), Duration.Inf))
  }

  def findByIdIO2(id: String): IO[Person] = {
    IO.executionContext.flatMap { implicit ec =>
      IO.async_(cb => findPersonById(id).onComplete{
        case Success(person) => cb(Right(person))
        case Failure(exception) => cb(Left(exception))
      })
    }
  }

  def findByIdIO3(id: String): IO[Person] = {
    IO.executionContext.flatMap { implicit ec =>
      IO.fromFuture(IO(findPersonById(id)))
    }
  }

  override def run(args: List[String]): IO[ExitCode] =
//    val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

    findByIdIO3("123")
//      .evalOn(ec)
      .debug()
      .as(ExitCode.Success)


  end run
end AsyncApp
