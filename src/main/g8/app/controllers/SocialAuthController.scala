package controllers

import akka.actor.ActorSystem
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.impl.exceptions.OAuth2StateException
import com.mohiva.play.silhouette.impl.providers._
import forms.SocialAuthForm
import javax.inject.Inject
import models.User
import models.services.UserService
import play.api.i18n.I18nSupport
import play.api.mvc.{ AbstractController, AnyContent, ControllerComponents, Request }
import utils.auth.DefaultEnv

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.language.postfixOps

/**
 * The social auth controller.
 *
 * @param components             The Play controller components.
 * @param silhouette             The Silhouette stack.
 * @param userService            The user service implementation.
 * @param authInfoRepository     The auth info service implementation.
 * @param socialProviderRegistry The social provider registry.
 * @param actorSystem            The actor system.
 * @param executionContext       The execution context.
 */

class SocialAuthController @Inject() (
  components: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  socialProviderRegistry: SocialProviderRegistry,
  actorSystem: ActorSystem
)(implicit executionContext: ExecutionContext) extends AbstractController(components) with I18nSupport with Logger {

  /**
   * OAuthInfoFromToken extracts the token from the request (as seen in the authenticate method of FacebookTokenProvider)
   * It then returns optionally an OAuth2Info with the info.
   */
  def authenticateToken(provider: String) = silhouette.UnsecuredAction.async { implicit request =>
    val authInfoData = SocialAuthForm.form.bindFromRequest.fold(
      form => None,
      data => Some(SocialAuthForm.toAuthToken(data))
    )
    ((socialProviderRegistry.get[SocialProvider](provider), authInfoData) match {
      case (Some(p: OAuth2Provider with CommonSocialProfileBuilder), Some(authInfo)) =>
        for {
          profile <- p.retrieveProfile(authInfo)
          user <- userService.save(User(profile))
          // We are using the user's email for the providerID rather than the numeric value returned by the provider.
          loginInfo <- Future.successful(LoginInfo(provider, user.email))
          authenticator <- silhouette.env.authenticatorService.create(loginInfo)
          value <- silhouette.env.authenticatorService.init(authenticator)
          result <- silhouette.env.authenticatorService.embed(value, Ok)
        } yield {
          actorSystem.scheduler.scheduleOnce(1 millis) {
            authInfoRepository.add(loginInfo, authInfo)
          }

          silhouette.env.eventBus.publish(LoginEvent(user, request))
          result
        }
      case (_, None) =>
        Future.failed(
          new OAuth2StateException(s"No token found in the request while authenticating with \$provider")
        )
      case _ =>
        Future.failed(new ProviderException(s"Cannot authenticate with unexpected social provider \$provider"))
    }).recover {
      case e: OAuth2StateException =>
        logger.error("Unexpected token error", e)
        NotFound
      case e: ProviderException =>
        logger.error("Unexpected provider error", e)
        NotFound
    }
  }

  /**
   * Authenticates a user against a social provider.
   *
   * @param provider The ID of the provider to authenticate against.
   * @return The result to display.
   */
  def authenticate(provider: String) = Action.async { implicit request: Request[AnyContent] =>
    (socialProviderRegistry.get[SocialProvider](provider) match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
        p.authenticate().flatMap {
          case Left(result) => Future.successful(result)
          case Right(authInfo) => for {
            profile <- p.retrieveProfile(authInfo)
            user <- userService.save(User(profile))
            authInfo <- authInfoRepository.save(profile.loginInfo, authInfo)
            authenticator <- silhouette.env.authenticatorService.create(profile.loginInfo)
            value <- silhouette.env.authenticatorService.init(authenticator)
            result <- silhouette.env.authenticatorService.embed(value, Ok("LOGGED_IN"))
          } yield {
            silhouette.env.eventBus.publish(LoginEvent(user, request))
            result
          }
        }
      case _ => Future.failed(new ProviderException(s"Cannot authenticate with unexpected social provider \$provider"))
    }).recover {
      case e: ProviderException =>
        logger.error("Unexpected provider error", e)
        BadRequest("UNEXPECTED_PROVIDER")
    }
  }
}
