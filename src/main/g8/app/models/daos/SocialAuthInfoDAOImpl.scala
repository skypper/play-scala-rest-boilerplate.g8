package models.daos

import com.google.inject.{ Inject, Singleton }
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import models.SocialAuthInfo
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.MySQLProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.ClassTag

/**
 * An implementation of the social auth info DAO which stores the data in the database.
 */
@Singleton
class SocialAuthInfoDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends DelegableAuthInfoDAO[OAuth2Info]
  with HasDatabaseConfigProvider[MySQLProfile] {
  import profile.api._

  override val classTag: ClassTag[OAuth2Info] = implicitly[reflect.ClassTag[OAuth2Info]]

  /**
   * The data store for the auth info.
   */
  val AuthInfos = TableQuery[SocialAuthInfoTable]

  /**
   * Finds the auth info which is linked with the specified login info.
   *
   * @param loginInfo The linked login info.
   * @return The retrieved auth info or None if no auth info could be retrieved for the given login info.
   */
  def find(loginInfo: LoginInfo): Future[Option[OAuth2Info]] = {
    db.run(AuthInfos.filter(_.provider === loginInfo.providerID).filter(_.email === loginInfo.providerKey)
      .result.headOption).map(_.map(_.authInfo))
  }

  /**
   * Adds new auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be added.
   * @param authInfo The auth info to add.
   * @return The added auth info.
   */
  def add(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = {
    db.run(AuthInfos += SocialAuthInfo(None, loginInfo, authInfo)).map(_ => authInfo)
  }

  /**
   * Updates the auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be updated.
   * @param authInfo The auth info to update.
   * @return The updated auth info.
   */
  def update(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = {
    db.run(AuthInfos.insertOrUpdate(SocialAuthInfo(None, loginInfo, authInfo))).map(_ => authInfo)
  }

  /**
   * Saves the auth info for the given login info.
   *
   * This method either adds the auth info if it doesn't exists or it updates the auth info
   * if it already exists.
   *
   * @param loginInfo The login info for which the auth info should be saved.
   * @param authInfo The auth info to save.
   * @return The saved auth info.
   */
  def save(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = {
    find(loginInfo).flatMap {
      case Some(_) => update(loginInfo, authInfo)
      case None => add(loginInfo, authInfo)
    }
  }

  /**
   * Removes the auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(loginInfo: LoginInfo): Future[Unit] = {
    db.run(
      AuthInfos.filter(_.provider === loginInfo.providerID).filter(_.email === loginInfo.providerKey).delete).map(_ => ())
  }

  protected class SocialAuthInfoTable(tag: Tag) extends Table[SocialAuthInfo](tag, "SOCIALAUTHINFO") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def provider = column[String]("PROVIDER")
    def email = column[String]("EMAIL", O.Unique)
    def accessToken = column[String]("ACCESS_TOKEN")
    def tokenType = column[String]("TOKEN_TYPE")
    def expiresIn = column[Int]("EXPIRES_IN")
    def refreshToken = column[String]("REFRESH_TOKEN")

    def * = (id.?, provider, email, accessToken, tokenType.?, expiresIn.?, refreshToken.?) <> ((SocialAuthInfo.mapperTo _).tupled, SocialAuthInfo.mapperFrom)
  }
}
