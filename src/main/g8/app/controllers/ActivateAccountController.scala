package controllers

import java.util.UUID

import com.mohiva.play.silhouette.api._
import javax.inject.Inject
import models.services.{ AuthTokenService, UserService }
import play.api.i18n.I18nSupport
import play.api.libs.mailer.MailerClient
import play.api.mvc.{ AbstractController, AnyContent, ControllerComponents, Request }
import utils.auth.DefaultEnv

import scala.concurrent.{ ExecutionContext, Future }

/**
 * The `Activate Account` controller.
 *
 * @param components       The Play controller components.
 * @param silhouette       The Silhouette stack.
 * @param userService      The user service implementation.
 * @param authTokenService The auth token service implementation.
 * @param mailerClient     The mailer client.
 * @param executionContext The execution context.
 */
class ActivateAccountController @Inject() (
  components: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  authTokenService: AuthTokenService,
  mailerClient: MailerClient
)(implicit executionContext: ExecutionContext) extends AbstractController(components) with I18nSupport {

  /**
   * Activates an account.
   *
   * @param token The token to identify a user.
   * @return The result to display.
   */
  def activate(token: UUID) = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    authTokenService.validate(token).flatMap {
      case Some(authToken) => userService.retrieve(authToken.userID).flatMap { user =>
        userService.save(user.get.copy(activated = true)).map { _ =>
          Ok("ACCOUNT_ACTIVATED")
        }
      }
      case None => Future.successful(BadRequest("INVALID_ACTIVATION_CODE"))
    }
  }
}
