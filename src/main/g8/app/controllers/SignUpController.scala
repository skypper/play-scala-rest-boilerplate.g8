package controllers

import java.util.UUID

import akka.actor.ActorSystem
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.impl.providers._
import forms.SignUpForm
import javax.inject.Inject
import models.User
import models.services.{ AuthTokenService, UserService }
import play.api.i18n.{ I18nSupport, Messages }
import play.api.libs.mailer.{ Email, MailerClient }
import play.api.mvc.{ AbstractController, AnyContent, ControllerComponents, Request }
import utils.auth.{ DefaultEnv, SimpleUser }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

/**
 * The `Sign Up` controller.
 *
 * @param components             The Play controller components.
 * @param silhouette             The Silhouette stack.
 * @param userService            The user service implementation.
 * @param authInfoRepository     The auth info repository implementation.
 * @param authTokenService       The auth token service implementation.
 * @param avatarService          The avatar service implementation.
 * @param passwordHasherRegistry The password hasher registry.
 * @param mailerClient           The mailer client.
 * @param actorSystem            The actor system.
 * @param executionContext       The execution context.
 */
class SignUpController @Inject() (
  components: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  authTokenService: AuthTokenService,
  avatarService: AvatarService,
  passwordHasherRegistry: PasswordHasherRegistry,
  mailerClient: MailerClient,
  actorSystem: ActorSystem
)(implicit executionContext: ExecutionContext) extends AbstractController(components) with I18nSupport {

  /**
   * Handles the submitted form.
   *
   * @return The result to display.
   */
  def submit = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    SignUpForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest("INVALID_FORM")),
      data => {
        val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
        userService.retrieve(loginInfo).flatMap {
          case Some(_) =>
            Future.successful(BadRequest("USER_EXISTS"))
          case None =>
            val user = User(
              id = None,
              userId = UUID.randomUUID(),
              name = data.firstName + " " + data.lastName,
              email = data.email,
              avatarURL = None,
              activated = false,
              failedLoginAttempts = 0,
              invitedBy = data.invitedBy,
              roles = SimpleUser.name
            )
            val authInfo = passwordHasherRegistry.current.hash(data.password)
            for {
              avatarURL <- avatarService.retrieveURL(data.email)
              user <- userService.save(user.copy(avatarURL = avatarURL))
            } yield {
              actorSystem.scheduler.scheduleOnce(1 millis) {
                authInfoRepository.add(loginInfo, authInfo)

                authTokenService.create(user.userId).map { authToken =>
                  val url = routes.ActivateAccountController.activate(authToken.id).absoluteURL()
                  mailerClient.send(Email(
                    subject = Messages("email.sign.up.subject"),
                    from = Messages("email.from"),
                    to = Seq(data.email),
                    bodyText = Some(views.txt.emails.signUp(user, url).body),
                    bodyHtml = Some(views.html.emails.signUp(user, url).body)
                  ))
                }
              }

              silhouette.env.eventBus.publish(SignUpEvent(user, request))
              Ok("SIGNED_UP")
            }
        }
      }
    )
  }
}
