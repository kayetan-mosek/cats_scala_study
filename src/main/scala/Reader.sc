import cats.*
import cats.data.*
import cats.implicits.*

val signReader: Reader[Int, String] =
  Reader(n =>
    if (n > 0) "positive"
    else if (n < 0) "negative"
    else "zero")

signReader.run(1)
signReader.run(-15)
signReader.run(0)

val parityReader: Reader[Int,String] = Reader {
  case i if i % 2 == 0 => "even"
  case _ => "odd"
}

parityReader.run(2)
parityReader.run(3)

val descriptionReader: Reader[Int,String] =
  for
    sign <- signReader
    parity <- parityReader
  yield s"$sign and $parity"

descriptionReader.run(1)