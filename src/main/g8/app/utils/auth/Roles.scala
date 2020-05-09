package utils.auth

import com.mohiva.play.silhouette.api.Authorization
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.User
import play.api.mvc.Request

import scala.concurrent.Future

/**
 * Check for authorization
 */
case class WithRole(role: Role) extends Authorization[User, CookieAuthenticator] {
  def isAuthorized[B](user: User, unusedAuthenticator: CookieAuthenticator)(implicit request: Request[B]): Future[Boolean] = user.roles.split(",") match {
    case list: Array[String] => Future.successful(list.contains(role.name))
    case _ => Future.successful(false)
  }

}
/**
 * Trait for all roles
 */
trait Role {
  def name: String
}

/**
 * Companion object
 */
object Role {

  def apply(role: String): Role = role match {
    case UserManager.name => UserManager
    case Admin.name => Admin
    case SimpleUser.name => SimpleUser
    case _ => Unknown
  }

  def unapply(role: Role): Option[String] = Some(role.name)

}

/**
 * Administration role
 */
object UserManager extends Role {
  val name = "usermanager"
}

/**
 * Administration role
 */
object Admin extends Role {
  val name = "admin"
}

/**
 * Normal user role
 */
object SimpleUser extends Role {
  val name = "user"
}

/**
 * The generic unknown role
 */
object Unknown extends Role {
  val name = "-"
}