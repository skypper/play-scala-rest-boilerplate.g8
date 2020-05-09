package models.services

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.Inject
import models.User
import models.daos.UserDAOImpl

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Handles actions to users.
 *
 * @param userDAO The user DAO implementation.
 * @param ex      The execution context.
 */
class UserServiceImpl @Inject() (userDAO: UserDAOImpl)(implicit ex: ExecutionContext) extends UserService {

  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param id The ID to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  def retrieve(id: UUID) = userDAO.find(id)

  /**
   * Retrieves a user that matches the specified login info.
   *
   * @param loginInfo The login info to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given login info.
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.find(loginInfo)

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User) = userDAO.save(user)

  /**
   * Increment the number of failed login attempts
   *
   * @param loginInfo The info for the user that tried to login with invalid credentials.
   */
  def incrementFailedLoginAttempts(loginInfo: LoginInfo): Future[Unit] = userDAO.incrementFailedLoginAttempts(loginInfo)

  def all(): Future[Seq[User]] = userDAO.all()

  def findById(id: Int): Future[Option[User]] = userDAO.findById(id)

  /**
   * Updates a user.
   *
   * @param user The user to update.
   * @return The updated user.
   */
  def update(user: User): Future[Unit] = userDAO.update(user)

  /**
   * Deletes a user by id.
   *
   * @param id The id of the user to delete.
   */
  def delete(id: Int): Future[Unit] = userDAO.delete(id)
}
