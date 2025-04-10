package effetsdemo

import cats.effect.*
import cats.implicits.*

import scala.concurrent.duration.*

object Memoizations extends IOApp:

  def processClients(clients: List[Client]): IO[Unit] =
    loadEmailTemplates().flatMap { templates =>
      clients.traverse_ { client =>
        processClient(client).handleErrorWith {
          case NegativeBalance =>
            val email = templates.buildEmailForClient("negative-balance", client)
            sendEmail(email)
          case _ =>
            val email = templates.buildEmailForClient("generic-error", client)
            sendEmail(email)
        }
      }
    }

  def processClients2(clients: List[Client]): IO[Unit] =
    clients.traverse_ { client =>
      processClient(client).handleErrorWith { error =>
        loadEmailTemplates().flatMap { templates =>
          error match
            case NegativeBalance =>
              val email = templates.buildEmailForClient("negative-balance", client)
              sendEmail(email)
            case _ =>
              val email = templates.buildEmailForClient("generic-error", client)
              sendEmail(email)
        }
      }
    }

  def processClients3(clients: List[Client]): IO[Unit] =
    loadEmailTemplates().memoize.flatMap { templatesIO =>
      clients.traverse_ { client =>
        processClient(client).handleErrorWith { error =>
          templatesIO.flatMap { templates =>
            error match
              case NegativeBalance =>
                val email = templates.buildEmailForClient("negative-balance", client)
                sendEmail(email)
              case _ =>
                val email = templates.buildEmailForClient("generic-error", client)
                sendEmail(email)
          }
        }
      }
    }

  // Long running computation
  def loadEmailTemplates(): IO[EmailTemplates] =
    IO.sleep(5.seconds) *>
      IO.println("Loading email templates ...") *>
      IO.pure { (templateId: String, client: Client) =>
        if (templateId == "negative-balance")
          Email(s"Dear ${client.name}: your account has a negative balance", List(client.emailAddress))
        else
          Email(s"Dear ${client.name}: there is a problem with your account", List(client.emailAddress))
      }

  def processClient(client: Client): IO[Unit] =
    IO.println(s"Processing ${client.name}")
//      IO.raiseError(NegativeBalance)

  def sendEmail(email: Email): IO[Unit] =
    IO.println("Sending email")

  trait EmailTemplates:
    def buildEmailForClient(templateId: String, client: Client): Email
  end EmailTemplates

  trait Error extends Throwable

  case class Client(name: String, emailAddress: String)

  case class Email(body: String, recipients: List[String])

  object NegativeBalance extends Error

  object AccountExpired extends Error

  override def run(args: List[String]): IO[ExitCode] =
    val clients = List(Client("Leandro", "leandro@mail.com"), Client("Martin", "martin@mail.com"))
    processClients3(clients).as(ExitCode.Success)
//    processClients2(clients).as(ExitCode.Success)
//    processClients(clients).as(ExitCode.Success)


  end run
end Memoizations

