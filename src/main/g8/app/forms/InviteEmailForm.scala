package forms

import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the email of the user to send invitation email.
 */
object InviteEmailForm {

  /**
   * A play framework form.
   */
  val form = Form(
    mapping(
      "email" -> email
    )(Data.apply)(Data.unapply)
  )

  /**
   * The form data.
   *
   * @param email The email of the user.
   */
  case class Data(
    email: String)
}
