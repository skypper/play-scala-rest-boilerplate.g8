package models

import com.mohiva.play.silhouette.api.util.PasswordInfo

case class CredentialsAuthInfo(id: Option[Int], email: String, passwordInfo: PasswordInfo)

object CredentialsAuthInfo {
  def mapperTo(id: Option[Int], email: String, passwordHasher: String, password: String, passwordSalt: Option[String]) = apply(id, email, PasswordInfo(passwordHasher, password, passwordSalt))

  def mapperFrom(credentialsAuthInfo: CredentialsAuthInfo) = Some((
    credentialsAuthInfo.id,
    credentialsAuthInfo.email,
    credentialsAuthInfo.passwordInfo.hasher,
    credentialsAuthInfo.passwordInfo.password,
    credentialsAuthInfo.passwordInfo.salt
  ))
}
