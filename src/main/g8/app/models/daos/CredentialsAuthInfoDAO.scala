package models.daos

import com.google.inject.{ Inject, Singleton }
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import models.CredentialsAuthInfo
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.MySQLProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.ClassTag

/**
 * An implementation of the credentials auth info DAO which stores the data in the database.
 */
@Singleton
class CredentialsAuthInfoDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends DelegableAuthInfoDAO[PasswordInfo]
  with HasDatabaseConfigProvider[MySQLProfile] {
  import profile.api._

  override val classTag: ClassTag[PasswordInfo] = implicitly[reflect.ClassTag[PasswordInfo]]

  /**
   * The data store for the auth info.
   */
  val Passwords = TableQuery[CredentialsAuthInfoTable]

  /**
   * Finds the auth info which is linked with the specified login info.
   *
   * @param loginInfo The linked login info.
   * @return The retrieved auth info or None if no auth info could be retrieved for the given login info.
   */
  def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    db.run(Passwords.findBy(_.email).applied(loginInfo.providerKey).result.headOption).map(_.map(_.passwordInfo))
  }

  /**
   * Adds new auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be added.
   * @param passwordInfo The auth info to add.
   * @return The added auth info.
   */
  def add(loginInfo: LoginInfo, passwordInfo: PasswordInfo): Future[PasswordInfo] = {
    db.run(Passwords += CredentialsAuthInfo(None, loginInfo.providerKey, passwordInfo)).map(_ => passwordInfo)
  }

  /**
   * Updates the auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be updated.
   * @param passwordInfo The auth info to update.
   * @return The updated auth info.
   */
  def update(loginInfo: LoginInfo, passwordInfo: PasswordInfo): Future[PasswordInfo] = {
    db.run(Passwords.insertOrUpdate(CredentialsAuthInfo(None, loginInfo.providerKey, passwordInfo))).map(_ => passwordInfo)
  }

  /**
   * Saves the auth info for the given login info.
   *
   * This method either adds the auth info if it doesn't exists or it updates the auth info
   * if it already exists.
   *
   * @param loginInfo The login info for which the auth info should be saved.
   * @param passwordInfo The auth info to save.
   * @return The saved auth info.
   */
  def save(loginInfo: LoginInfo, passwordInfo: PasswordInfo): Future[PasswordInfo] = {
    find(loginInfo).flatMap {
      case Some(_) => update(loginInfo, passwordInfo)
      case None => add(loginInfo, passwordInfo)
    }
  }

  /**
   * Removes the auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(loginInfo: LoginInfo): Future[Unit] = {
    db.run(Passwords.findBy(_.email).applied(loginInfo.providerKey).delete).map(_ => ())
  }

  protected class CredentialsAuthInfoTable(tag: Tag) extends Table[CredentialsAuthInfo](tag, "CREDENTIALSAUTHINFO") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def email = column[String]("EMAIL", O.Unique)
    def passwordHasher = column[String]("PASSWORD_HASHER")
    def password = column[String]("PASSWORD")
    def passwordSalt = column[String]("PASSWORD_SALT")

    def * = (id.?, email, passwordHasher, password, passwordSalt.?) <> ((CredentialsAuthInfo.mapperTo _).tupled, CredentialsAuthInfo.mapperFrom)
  }
}
