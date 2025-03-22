import cats.effect.{ExitCode, IO, IOApp, Resource}

import java.io.*


object TutoTypelevel extends IOApp:

  def transmit(origin: InputStream, destination: OutputStream, buffer: Array[Byte], acc: Long): IO[Long] =
    for
      amount <- IO.blocking(origin.read(buffer, 0, buffer.length))
      count <-
        if (amount > -1)
          IO.blocking(destination.write(buffer, 0, amount)) >>
            transmit(origin, destination, buffer, acc + amount)
        else IO.pure(acc)
    yield count

  override def run(args: List[String]): IO[ExitCode] =
    for
      _ <-
        if (args.length > 2)
          IO.raiseError(new IllegalArgumentException("Need only origin and destination"))
        else IO.unit
      orig = new File(args.head)
      dest = new File(args(1))
      count <- copy(orig, dest)
      _ <- IO.println(s"$count bytes copied from ${orig.getPath} to ${dest.getPath}")
    yield ExitCode.Success

  def copy(origin: File, destination: File): IO[Long] =
    inputOutputStream(origin, destination).use { (in, out) =>
      transfer(in, out)
    }

  def inputOutputStream(in: File, out: File): Resource[IO, (FileInputStream, FileOutputStream)] =
    for
      inStream <- inputStream(in)
      outStream <- outputStream(out)
    yield (inStream, outStream)

  private def inputStream(f: File): Resource[IO, FileInputStream] =
    Resource.fromAutoCloseable(IO.blocking(new FileInputStream(f))) // simpler but without logging or control on closing op

  private def outputStream(f: File): Resource[IO, FileOutputStream] =
    Resource.make(IO.blocking(new FileOutputStream(f))) {
      outStream => IO.blocking(outStream.close()).attempt.void
    }

  def transfer(origin: InputStream, destination: OutputStream): IO[Long] = ???

end TutoTypelevel

