package controllers

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.{ Clock, Credentials }
import com.mohiva.play.silhouette.impl.providers._
import forms.SignInForm
import javax.inject.Inject
import models.services.UserService
import net.ceedubs.ficus.Ficus._
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{ AbstractController, AnyContent, ControllerComponents, Request }
import utils.auth.DefaultEnv

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

/**
 * The `Sign In` controller.
 *
 * @param components             The Play controller components.
 * @param silhouette             The Silhouette stack.
 * @param userService            The user service implementation.
 * @param credentialsProvider    The credentials provider.
 * @param configuration          The Play configuration.
 * @param clock                  The clock instance.
 * @param executionContext       The execution context.
 */
class SignInController @Inject() (
  components: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  credentialsProvider: CredentialsProvider,
  configuration: Configuration,
  clock: Clock)(implicit executionContext: ExecutionContext) extends AbstractController(components) with I18nSupport {

  /**
   * Handles the submitted form.
   *
   * @return The result to display.
   */
  def submit = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    SignInForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest("INVALID_FORM")),
      data => {
        val credentials = Credentials(data.email, data.password)
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
          userService.retrieve(loginInfo).flatMap {
            case Some(user) if !user.activated =>
              Future.successful(Unauthorized("USER_NOT_ACTIVATED"))
            case Some(user) if user.failedLoginAttempts >= 3 =>
              Future.successful(Unauthorized("USER_BLOCKED"))
            case Some(user) =>
              val c = configuration.underlying
              silhouette.env.authenticatorService.create(loginInfo).map {
                case authenticator if data.rememberMe =>
                  authenticator.copy(
                    expirationDateTime = clock.now + c.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry"),
                    idleTimeout = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout"),
                    cookieMaxAge = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.cookieMaxAge")
                  )
                case authenticator => authenticator
              }.flatMap { authenticator =>
                silhouette.env.eventBus.publish(LoginEvent(user, request))
                silhouette.env.authenticatorService.init(authenticator).flatMap { v =>
                  silhouette.env.authenticatorService.embed(v, Ok("SIGNED_IN"))
                }
              }
            case None => Future.successful(Unauthorized("USER_NOT_FOUND"))
          }
        }.recover {
          case _: ProviderException =>
            // Fetch the user and increase by 1 the number of failed login attempts.
            val loginInfo = LoginInfo(providerID = CredentialsProvider.ID, providerKey = data.email)
            userService.incrementFailedLoginAttempts(loginInfo)
            Unauthorized("INVALID_CREDENTIALS")
        }
      }
    )
  }
}
