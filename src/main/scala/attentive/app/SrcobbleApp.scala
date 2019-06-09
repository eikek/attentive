package attentive.app

import attentive.app.ops._

trait ScrobbleApp[F[_]] {

  def init: F[Unit]

  def register(reg: Register.Data): F[Register.Result]

  def newInvite(pass: String): F[NewInvite.Result]

  def accountExists(name: String): F[Boolean]

  def login(token: Login.Token): F[Login.Result]

  def loginUserPass(user: String, pass: String): F[Login.Result]

  def loginSession(key: String): F[Login.Result]

  def basicStats: F[BasicStats.Result]

  def userApp(account: String): ScrobbleUserApp[F]
}
