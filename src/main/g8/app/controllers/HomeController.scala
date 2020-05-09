package controllers

import com.mohiva.play.silhouette.api.Silhouette
import javax.inject._
import play.api.mvc._
import utils.auth.DefaultEnv

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() (
  val controllerComponents: ControllerComponents,
  silhouette: Silhouette[DefaultEnv]
) extends BaseController {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok("<html><body>Hello world! <a href='http://trips.toptal.com/signup/2'>Sign Up!</a></body></html>").as(HTML)
  }
}
