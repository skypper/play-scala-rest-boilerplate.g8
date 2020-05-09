package models.daos

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.Inject
import models.User
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.MySQLProfile

import scala.concurrent.{ ExecutionContext, Future }

class UserDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[MySQLProfile] with UserDAO {

  import profile.api._

  val Users = TableQuery[UserTable]

  def all(): Future[Seq[User]] = db.run(Users.result)

  def findById(id: Int): Future[Option[User]] = {
    db.run(Users.findBy(_.id).applied(id).result.headOption)
  }

  def find(userId: UUID): Future[Option[User]] = {
    db.run(Users.findBy(_.userId).applied(userId).result.headOption)
  }

  def find(loginInfo: LoginInfo): Future[Option[User]] = {
    db.run(Users
      .filter(_.email === loginInfo.providerKey).result.headOption)
  }

  def save(user: User): Future[User] = {
    val loginInfo = LoginInfo(CredentialsProvider.ID, user.email)
    find(loginInfo).map {
      case Some(_user) => update(user.copy(id = _user.id, userId = _user.userId))
      case None => db.run((Users += user))
    }.map { _ => user }
  }

  def update(user: User): Future[Unit] = {
    require(user.id.isDefined)
    db.run(Users.insertOrUpdate(user)).map(_ => ())
  }

  def delete(id: Int): Future[Unit] = {
    db.run(Users.findBy(_.id).applied(id).delete).map(_ => ())
  }

  def incrementFailedLoginAttempts(loginInfo: LoginInfo): Future[Unit] = {
    require(loginInfo.providerID == CredentialsProvider.ID)
    require(!loginInfo.providerKey.isEmpty)

    val a = (for {
      ns <- Users.filter(_.email === loginInfo.providerKey).map(_.failedLoginAttempts).result
      _ <- DBIO.seq(
        ns.map(n => Users.filter(_.email === loginInfo.providerKey).map(_.failedLoginAttempts).update(n + 1)): _*)
    } yield ()).transactionally
    db.run(a).map(_ => ())
  }

  class UserTable(tag: Tag) extends Table[User](tag, "USER") {

    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def userId = column[UUID]("USER_ID", O.Unique, O.SqlType("UUID"))
    def name = column[String]("NAME")
    def email = column[String]("EMAIL", O.Unique)
    def avatarURL = column[String]("AVATAR_URL")
    def activated = column[Boolean]("ACTIVATED")
    def failedLoginAttempts = column[Int]("FAILED_LOGIN_ATTEMPTS")
    def invitedBy = column[Int]("INVITED_BY")
    def roles = column[String]("ROLES")

    def * = (
      id.?,
      userId,
      name,
      email,
      avatarURL.?,
      activated,
      failedLoginAttempts,
      invitedBy.?,
      roles) <> ((User.mapperTo _).tupled, User.mapperFrom)
  }

}
