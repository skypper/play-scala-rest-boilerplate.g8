package forms

import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the social authentication process.
 */
object SocialAuthForm {

  /**
   * A play framework form.
   */
  val form = Form(
    mapping(
      "accessToken" -> nonEmptyText,
      "tokenType" -> optional(text),
      "expiresIn" -> optional(number),
      "refreshToken" -> optional(text)
    )(Data.apply)(Data.unapply)
  )

  /**
   * The form data.
   *
   * @param accessToken  The access token.
   * @param tokenType    The token type.
   * @param expiresIn    The number of seconds before the token expires.
   * @param refreshToken The refresh token.
   */
  case class Data(
    accessToken: String,
    tokenType: Option[String] = None,
    expiresIn: Option[Int] = None,
    refreshToken: Option[String] = None)

  def toAuthToken(formData: Data) =
    OAuth2Info(formData.accessToken, formData.tokenType, formData.expiresIn, formData.refreshToken, None)
}
