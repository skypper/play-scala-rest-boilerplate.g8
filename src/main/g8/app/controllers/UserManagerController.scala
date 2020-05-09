package controllers

import akka.actor.ActorSystem
import com.mohiva.play.silhouette.api.{ LoginInfo, Silhouette }
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.Inject
import models.User
import models.services.UserService
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.libs.mailer.MailerClient
import play.api.mvc.{ AbstractController, ControllerComponents }
import utils.Logger
import utils.auth._

import scala.concurrent.{ ExecutionContext, Future }

class UserManagerController @Inject() (
  userService: UserService,
  silhouette: Silhouette[DefaultEnv],
  cc: ControllerComponents,
  mailerClient: MailerClient,
  actorSystem: ActorSystem,
  assets: Assets,
  assetsFinder: AssetsFinder)(implicit executionContext: ExecutionContext)
  extends AbstractController(cc) with Logger with I18nSupport {
  def list() = silhouette.SecuredAction(WithRole(UserManager) || WithRole(Admin)).async { implicit request =>
    userService.all()
      .map { res =>
        Ok(Json.toJson(res))
      }
  }

  def detail(id: Int) = silhouette.SecuredAction(WithRole(UserManager) || WithRole(Admin)).async { implicit request =>
    userService.findById(id).map {
      case Some(returnedUser) => Ok(Json.toJson(returnedUser))
      case None => NotFound
    }
  }

  def update() = silhouette.SecuredAction(WithRole(UserManager) || WithRole(Admin))(parse.json) { implicit request =>
    val userDataResult = request.body.validate[User]
    userDataResult.fold(
      { errors =>
        logger.error(errors.toString())
        BadRequest("INVALID_FORM")
      },
      { form: User =>
        userService.update(form)
        Ok(Json.toJson(form)).as(JSON)
      }
    )
  }

  def delete(id: Int) = silhouette.SecuredAction(WithRole(UserManager) || WithRole(Admin)).async {
    userService.delete(id).map { _ => Ok }
  }

  def current() = silhouette.SecuredAction(WithRole(SimpleUser) || WithRole(Admin)).async { implicit request =>
    userService.retrieve(request.identity.userId).map { returnedUser =>
      if (returnedUser.isEmpty) {
        BadRequest("USER_NOT_EXISTS")
      }
      Ok(Json.toJson(returnedUser))
    }
  }

  def updateCurrent() = silhouette.SecuredAction(WithRole(SimpleUser)).async(parse.json) { implicit request =>
    val userDataResult = request.body.validate[User]
    userDataResult.fold(
      { errors =>
        logger.error(errors.toString())
        Future.successful(BadRequest("INVALID_FORM"))
      },
      { form: User =>
        userService.retrieve(LoginInfo(CredentialsProvider.ID, form.email)).map {
          case Some(_) =>
            userService.update(form)
            Ok(Json.toJson(form)).as(JSON)
          case None =>
            Unauthorized
        }
      }
    )
  }
}
