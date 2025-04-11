package effetsdemo

import cats.*
import cats.implicits.*
import cats.effect.*
import cats.effect.implicits.*

import scala.concurrent.duration.*

object TimedApp extends IOApp:

  case class Token(value:String, expirationTimeInMillis: Long):
    def isExpired(): IO[Boolean] =
      IO.realTime.map(_.toMillis > expirationTimeInMillis)
  end Token


  def measure[A](ioa: IO[A]): IO[FiniteDuration] =
    for
      start <- IO.monotonic
      _ <- ioa
      end <- IO.monotonic
    yield end - start

  override def run(args: List[String]): IO[ExitCode] =
    val program = (1 to 100).toList.traverse_ { i =>
      IO.println(i)
    }
    measure(program)
      .map(_.toMillis)
      .debug()
      .as(ExitCode.Success)
//    for
//      currentTime <- IO.realTime
//      token = Token("123", (currentTime + FiniteDuration(10,SECONDS)).toMillis)
//      isExpired <- token.isExpired()
//      _ <- IO.println(s"Is expired : $isExpired")
//    yield ExitCode.Success







  end run
end TimedApp

