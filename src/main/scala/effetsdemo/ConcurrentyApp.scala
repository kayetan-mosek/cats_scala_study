package effetsdemo

import cats.*
import cats.implicits.*
import cats.effect.*
import cats.effect.implicits.*

import scala.concurrent.duration.*

object ConcurrentyApp extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =

    /** parMapN
    case class Image(bytes: List[Byte])

    def httpImages(n:Int): IO[List[Image]] =
      IO.sleep(100.millis) *> (1 to n).toList.map(i => Image(List(i.toByte))).pure[IO]

    def dbImages(n:Int): IO[List[Image]] =
      IO.sleep(100.millis) *> (1 to n).toList.map(i => Image(List((10+i).toByte))).pure[IO]


    val n = 50

    (httpImages(n), dbImages(n)).parMapN { case (httpImages, dbImages) => httpImages ++ dbImages}
      .debug().as(ExitCode.Success)
     */

    /* traversePar
    case class Person(name: String)

    def save(person:Person): IO[Long]= IO.sleep(100.millis) *> person.name.length.toLong.pure[IO]


//    val people = List(Person("leandro"),Person("martin"), Person("max"))
    val people = (1 to 50).toList.map(i => Person(i.toString))
    people
      //.map(save)
      //.sequence
//      .traverse(save)
      .parTraverse(save)
      .debug()
      .as(ExitCode.Success)

  */

    case class Image(bytes:List[Byte])

    def fetchHttp(n:Int): IO[List[Image]] =
      IO.sleep(1000.millis) *> (1 to n).toList.map(i => Image(List(i.toByte))).pure[IO]


    def fetchDb(n: Int): IO[List[Image]] =
      IO.sleep(100.millis) *> (1 to n).toList.map(i => Image(List((100 + i).toByte))).pure[IO]

    val n = 50

    IO.race(fetchHttp(n), fetchDb(n)).map {
      case Right(dbImgs) => s"Db won: $dbImgs"
      case Left(httpImgs) => s"Http won: $httpImgs"
    }
      .debug()
      .as(ExitCode.Success)


end ConcurrentyApp

