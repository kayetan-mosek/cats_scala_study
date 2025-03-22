import cats.*
import cats.data.*
import cats.implicits.*

// State ==~ S => (S, A)

//State[Int, Double](s => (s + 1, s.toDouble * 2))
////
////type St[A] = State[Int, A]
////
////8.pure[St]
////
////val x = State[Int, Double](s => (s + 1, s.toDouble * 3))
////x.run(4).value
////x.runA(4).value
////x.runS(4).value
////
////State.get[Int].run(5).value
////State.set[Int](10).run(1).value
////State.modify[Int](s => s * 5).run(3).value
////State.inspect[Int, String](s => s.toString + "!").run(2).value
////
////def get[S]: State[S, S] = ???
////def set[S](s: S): State[S, Unit] = ???
////def modify[S](f: S => S): State[S, Unit] = ???
////def inspect[S, T](f: S => T): State[S, T] = ???

val r = State[Int, Double](i => (i + 64, (i.toDouble +7)))
val y = State[Int, Double](i => (0, (i.toDouble+ 3)))
//r.flatMap(d => y).run(1).value
r.run(1).value
(r,y).mapN((a,z) => {
  println(a)
  println(z)
  a + z
}).run(1).value
//(r,y).mapN((a,z) => a + z).runEmpty.value
//r.runEmpty.value
//y.runEmpty.value
//r.flatMap(rs => y.modify(_ +666).map(d => d + rs)).run(1).value