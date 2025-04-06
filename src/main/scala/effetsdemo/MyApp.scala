package effetsdemo

import cats.data.*
import cats.data.Validated.{Invalid, Valid}
import cats.effect.*
import cats.implicits.*
import scala.util.control.NonFatal

object Controller:

  import Validations.*

  def posTransfer(request: Request): IO[Response] =
    val response =
      (validateAccountNumber(request.fromAccount),
        validateAccountNumber(request.toAccount),
        validateDouble(request.amount))
        .tupled match {
        case Valid((from, to, amount)) =>
          Service.transfer(from, to, amount).map {
            case Right(()) => Response(200, "Transfer successfully executed")
            case Left(err) => Response(400, err.toString)
          }
        case Invalid(err) =>
          Response(400, err.mkString_(", ")).pure[IO]
      }
    response.handleErrorWith{
      case NonFatal(e) => Response(500, "Internal server error").pure[IO]
    }

  case class Request(fromAccount: String, toAccount: String, amount: String)

  case class Response(status: Int, body: String)

end Controller

object ErrorHandlingApp extends IOApp:

  import Controller.*
  import Models.*
  import Repository.*

  override def run(args: List[String]): IO[ExitCode] =
    val request = Request("12345", "56789", "2000")

    saveAccount(Account("12345", 10000)).flatMap { _ =>
      saveAccount(Account("56789", 2000)).flatMap { _ =>
        posTransfer(request)
          .flatTap(IO.println)
          .as(ExitCode.Success)
      }
    }

  end run
end ErrorHandlingApp

object Validations:
  type Valid[A] = ValidatedNec[String, A]

  def validateDouble(s: String): Valid[Double] =
    s.toDoubleOption.toValidNec("String is not a parseable double")

  def validateAccountNumber(accountNumber: String): Valid[String] =
    Validated.condNec(
      accountNumber.forall(_.isLetterOrDigit),
      accountNumber,
      s"The account number $accountNumber must only contain letters or digits"
    )

end Validations

trait DomainError

case class InsufficientBalanceError(actualBalance: Double, amountToWithdraw: Double) extends DomainError

case class MaximumBalanceExceededError(actualBalance: Double, amountToDeposit: Double) extends DomainError

case class AccountNotFound(accountNumber: String) extends DomainError

object Models:
  val maxBalance = 5000

  case class Account(number: String, balance: Double):
    def withdraw(amount: Double): Either[DomainError, Account] =
      if (amount <= balance)
        Right(this.copy(balance = balance - amount))
      else
        Left(InsufficientBalanceError(balance, amount))

    def deposit(amount: Double): Either[DomainError, Account] =
      if (amount + balance >= maxBalance)
        Left(MaximumBalanceExceededError(balance, amount))
      else
        Right(this.copy(balance = balance + amount))

  end Account

end Models

object Repository:

  import Models.*

  var data = Map.empty[String, Account]

  def findAccountByNumber(number: String): IO[Option[Account]] =
    data.get(number).pure[IO]

  def saveAccount(account: Account): IO[Unit] =
    IO {
      data = data + (account.number -> account)
    }

end Repository

object Service:

  import Models.*
  import Repository.*

  def transfer(fromAccountNumber: String, toAccountNumber: String, amount: Double): IO[Either[DomainError, Unit]] =
    findAccountByNumber(fromAccountNumber).flatMap { fromAccOpt =>
      findAccountByNumber(toAccountNumber).flatMap { toAccOpt =>
        val accounts: Either[DomainError, (Account, Account)] =
          for
            fromAcc <- fromAccOpt.toRight(AccountNotFound(fromAccountNumber))
            toAcc <- toAccOpt.toRight(AccountNotFound(toAccountNumber))
            updatedFromAccount <- fromAcc.withdraw(amount)
            updatedToAccount <- toAcc.deposit(amount)
          yield (updatedFromAccount, updatedToAccount)

        accounts.traverse { case (fromAcc, toAcc) =>
          saveAccount(fromAcc) *> saveAccount(toAcc)
        }
      }
    }

end Service
