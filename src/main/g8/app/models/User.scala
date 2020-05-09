package models

import java.util.UUID

import com.mohiva.play.silhouette.api.Identity
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import play.api.libs.json.Json
import utils.auth.SimpleUser

case class User(
  val id: Option[Int],
  val userId: UUID,
  val name: String,
  val email: String,
  val avatarURL: Option[String],
  val activated: Boolean,
  val failedLoginAttempts: Int,
  val invitedBy: Option[Int],
  val roles: String) extends Identity

object User {
  implicit val writesFormat = Json.format[User]

  def mapperTo(
    id: Option[Int],
    userId: UUID,
    name: String,
    email: String,
    avatarURL: Option[String],
    activated: Boolean,
    failedLoginAttempts: Int,
    invitedBy: Option[Int],
    roles: String) = apply(
    id,
    userId,
    name,
    email,
    avatarURL,
    activated,
    failedLoginAttempts,
    invitedBy,
    roles)

  def mapperFrom(user: User) = Some((
    user.id,
    user.userId,
    user.name,
    user.email,
    user.avatarURL,
    user.activated,
    user.failedLoginAttempts,
    user.invitedBy,
    user.roles))

  def apply(profile: CommonSocialProfile): User = User(
    id = None,
    userId = UUID.randomUUID(),
    name = profile.fullName.getOrElse(profile.firstName.get + " " + profile.lastName.get),
    email = profile.email.get,
    avatarURL = profile.avatarURL,
    activated = true,
    failedLoginAttempts = 0,
    invitedBy = None,
    roles = SimpleUser.name
  )
}