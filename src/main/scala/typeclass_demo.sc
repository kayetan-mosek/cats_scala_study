import java.io.FileOutputStream
import java.nio.ByteBuffer
import scala.util.Using

trait ByteEncoder[A] {
  def encode(a: A): Array[Byte]
}

trait Channel {
  def write[A: ByteEncoder](obj: A): Unit
}

object FileChannel extends Channel:
  override def write[A: ByteEncoder](obj: A): Unit =
    val bytes: Array[Byte] = implicitly[ByteEncoder[A]].encode(obj)
    println("writing")
    Using(new FileOutputStream("fp-course/test")) { os =>
      os.write(bytes)
      os.flush()
    }

//object IntByteEncoder extends ByteEncoder[Int] {
//  override def encode(a: Int): Array[Byte] =
//    val bb = ByteBuffer.allocate(4)
//    bb.putInt(a)
//    bb.array()
//}
//
implicit val IntEncoderOps: ByteEncoder[Int] = (a: Int) =>
  val bb = ByteBuffer.allocate(4)
  bb.putInt(a)
  bb.array()

implicit val StringEncoderOps: ByteEncoder[String] = (s: String) =>
  s.getBytes

FileChannel.write(5)
FileChannel.write("5")
