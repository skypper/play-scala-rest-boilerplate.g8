package models.daos

import com.mohiva.play.silhouette.api.LoginInfo
import models.User

import scala.concurrent.Future

/**
 * Give access to the user object.
 */
trait UserDAO {
  def all(): Future[Seq[User]]

  def findById(id: Int): Future[Option[User]]

  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(loginInfo: LoginInfo): Future[Option[User]]

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User): Future[User]

  /**
   * Updates a user.
   *
   * @param user The user to update.
   * @return The updated user.
   */
  def update(user: User): Future[Unit]

  /**
   * Deletes a user by id.
   *
   * @param id The id of the user to delete.
   */
  def delete(id: Int): Future[Unit]

  /**
   * Increment the number of failed login attempts
   *
   * @param loginInfo The info for the user that tried to login with invalid credentials.
   * @return The updated user.
   */
  def incrementFailedLoginAttempts(loginInfo: LoginInfo): Future[Unit]
}
