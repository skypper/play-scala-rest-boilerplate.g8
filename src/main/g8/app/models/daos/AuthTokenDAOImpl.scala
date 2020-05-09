package models.daos

import java.time.ZonedDateTime
import java.util.UUID

import com.google.inject.Inject
import models.AuthToken
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.MySQLProfile

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * An implementation of the auth token DAO which stores the data in the database.
 */
class AuthTokenDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends AuthTokenDAO
  with HasDatabaseConfigProvider[MySQLProfile] {
  import profile.api._

  /**
   * The data store for the auth info.
   */
  val AuthTokens = TableQuery[AuthTokenTable]

  /**
   * Finds a token by its ID.
   *
   * @param id The unique token ID.
   * @return The found token or None if no token for the given ID could be found.
   */
  def find(id: UUID) = {
    db.run(AuthTokens.findBy(_.tokenId).applied(id).result.headOption)
  }

  /**
   * Finds expired tokens.
   *
   * @param dateTime The current date time.
   */
  def findExpired(dateTime: ZonedDateTime) = {
    db.run(AuthTokens.filter(_.expiry < dateTime).result)
  }

  /**
   * Saves a token.
   *
   * @param token The token to save.
   * @return The saved token.
   */
  def save(token: AuthToken) = {
    db.run(AuthTokens += token).map { _ => token }
  }

  /**
   * Removes the token for the given ID.
   *
   * @param id The ID for which the token should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(id: UUID) = {
    db.run(AuthTokens.findBy(_.tokenId).applied(id).delete).map { _ => () }
  }

  protected class AuthTokenTable(tag: Tag) extends Table[AuthToken](tag, "AUTHTOKEN") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def tokenId = column[UUID]("TOKEN_ID", O.Unique, O.SqlType("UUID"))
    def userId = column[UUID]("USER_ID", O.Unique, O.SqlType("UUID"))
    def expiry = column[ZonedDateTime]("EXPIRY")

    def * = (tokenId, userId, expiry) <> ((AuthToken.mapperTo _).tupled, AuthToken.mapperFrom)
  }
}
