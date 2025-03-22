
class Account(val id: Long, val balance: Double)

def upd: Account => Account = acc => Account(acc.id, acc.balance + 100.0)

class DbClient:
  def executeStatement(stm:String): Boolean = ???
  def executeQuery(q:String): String = ???

class AccountService:

  def find(id: Long): DbClient => Account =  db =>
    val acc = db.executeQuery(s"SELECT * FROM account_table WHERE id = $id")
    Account(acc.toInt,acc.toDouble)

  def save(acc: Account): DbClient => Boolean = db =>
    ???

  def update(id: Int, f: Account => Account, db: DbClient): Boolean = ???


object Playground extends App:
  val c = new AccountService()
  c.find(123)(new DbClient)
  c.save(new Account(1,123))(new DbClient)