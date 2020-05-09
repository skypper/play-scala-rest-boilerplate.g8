package models.services

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import models.User

import scala.concurrent.Future

/**
 * Handles actions to users.
 */
trait UserService extends IdentityService[User] {

  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param id The ID to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  def retrieve(id: UUID): Future[Option[User]]

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User): Future[User]

  /**
   * Increment the number of failed login attempts
   *
   * @param loginInfo The info for the user that tried to login with invalid credentials.
   */
  def incrementFailedLoginAttempts(loginInfo: LoginInfo): Future[Unit]

  def all(): Future[Seq[User]]

  def findById(id: Int): Future[Option[User]]

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
}
