import cats.*
import cats.data.*
import cats.effect.*
import cats.implicits.*



//import cats.effect.unsafe.implicits.global

trait AccountRepo

type ErrorOr[A] = Either[String,A]
type ErrorOrL[A] = Either[List[String],A]
type AccountOp[A] = ReaderT[ErrorOr, AccountRepo, A]
val dummyRepo = new AccountRepo {}

type ErrorOrOpt[A] = OptionT[ErrorOr, A]
type AccountOpOpts[A] = ReaderT[ErrorOrOpt, AccountRepo, A]

// impl
val s: AccountOp[Int] = ReaderT(r => Either.right(42))
val ff: AccountOp[Int] = ReaderT(_ => Left("MyStr value"))
val ff2: AccountOp[Int] = ReaderT(_ => 6666.asRight[String])
val kk = for
  si <- s
  ffi <- ff2
yield si + ffi

s.map(_+1).run(dummyRepo)
s.flatMapF(f => Either.right[String,Int](f+23)).run(dummyRepo)
s.flatMap(f => ReaderT(_ => Either.right[String,Int](f+23))).run(dummyRepo)
s.mapF(f => f.map(_+23)).run(dummyRepo)
println("waouh.?")


println("waouh.?")
ff.mapK(new (ErrorOr ~> ErrorOrL) {
  override def apply[A](fa: ErrorOr[A]): ErrorOrL[A] = fa match
    case Right(i) => Right(i)
    case Left(s) => Left(List(s))
}).run(dummyRepo)

println("sep")
(s |+| ff2).run(dummyRepo)

kk.run(dummyRepo)
//s.run(dummyRepo)
//
//val o2: AccountOpOpts[Int] = ReaderT(r => OptionT(Option(2).asRight[String]))
//val o1: AccountOpOpts[Int] = ReaderT(r => OptionT(Either.right(Option(42))))
//val o3: AccountOpOpts[Int] = ReaderT(r => OptionT(Either.right(None)))
//
//o1.run(dummyRepo)
//o2.run(dummyRepo)
//o3.run(dummyRepo)

OptionT(Option(2).asRight[String]).map(s => s + 2)
OptionT(Option(2).asRight[String]).map(s => s + 2)
OptionT(Option(6).asRight[String]).flatMap(s => OptionT(Option(s+2).asRight[String]))
OptionT(Option(6).asRight[String]).flatMap(i => (i+1).pure[ErrorOrOpt])
66.pure[ErrorOrOpt].flatMap(i => (i+1).pure[ErrorOrOpt])
66.pure[ErrorOrOpt].subflatMap(i => Option(i+213))
66.pure[ErrorOrOpt].semiflatMap(i => Right(i+1))
66.pure[ErrorOrOpt].flatMapF(n => Right(Some(n+2)))
66.pure[ErrorOrOpt].flatMapF(n => Either.right(Option(n+42)))
66.pure[ErrorOrOpt].flatMapF(n => Option(n+1).pure[ErrorOr])