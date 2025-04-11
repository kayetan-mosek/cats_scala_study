package effetsdemo

import cats.*
import cats.effect.*
import cats.effect.implicits.*
import cats.implicits.*

import scala.concurrent.duration.*

object DeferredApp extends IOApp:

  case class Item(id:Long)

  def loadItems():IO[List[Item]]=
    IO.println("Loading items") *>
    IO.sleep(2.seconds) *>
    IO.println("Items loaded") *>
    IO(List(Item(1), Item(2)))
//    IO.raiseError(new Exception("Failed to load items"))

  def initUi():IO[Unit]=
    IO.println("Init UI") *>
      IO.sleep(2.seconds) *>
      IO.println("UI initialized")

  def showItems(items:List[Item]): IO[Unit] =
    IO.println("Showing items")

  def showError(): IO[Unit] =
    IO.println("Showing error")

  def setupUi(): IO[Unit] =
    // This will load sequentially
//    initUi() *> loadItems().flatMap(items => showItems(items)).handleErrorWith(_ => showError())
    // This will load in parallel, but in case of error, initUi will be interrupted
//    (initUi(), loadItems()).parMapN { case (_,items) =>
//      showItems(items)
//    }.flatten
//      .handleErrorWith(_ => showError())
    // This will load in parallel, and initui will not be interrupted in case of error in items loading
//      (initUi(), loadItems().attempt).parMapN {
//          case (_,Right(items)) => showItems(items)
//          case (_,Left(err)) => showError()
//      }.flatten
    Deferred[IO,Either[Throwable, List[Item]]].flatMap { defItems =>
      List(handleUi(defItems), handleItems(defItems)).parSequence_
    }

  def handleUi(defItems: Deferred[IO,Either[Throwable, List[Item]]]): IO[Unit] =
    initUi() *> defItems.get.flatMap {
      case Right(items) => showItems(items)
      case Left(err) => showError()
    }


  def handleItems(defItems: Deferred[IO,Either[Throwable, List[Item]]]): IO[Unit] =
    loadItems()
      .flatMap(items => defItems.complete(Right(items)))
      .handleErrorWith(e => defItems.complete(Left(e)))
      .void

  override def run(args: List[String]): IO[ExitCode] =
    setupUi().as(ExitCode.Success)







  end run
end DeferredApp



