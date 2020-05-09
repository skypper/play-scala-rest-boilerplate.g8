package models

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info

case class SocialAuthInfo(id: Option[Int], loginInfo: LoginInfo, authInfo: OAuth2Info)

object SocialAuthInfo {
  def mapperTo(
    id: Option[Int],
    email: String,
    provider: String,
    accessToken: String,
    tokenType: Option[String] = None,
    expiresIn: Option[Int] = None,
    refreshToken: Option[String] = None) =
    apply(id, LoginInfo(provider, email), OAuth2Info(accessToken, tokenType, expiresIn, refreshToken))

  def mapperFrom(socialAuthInfo: SocialAuthInfo) = Some((
    socialAuthInfo.id,
    socialAuthInfo.loginInfo.providerID,
    socialAuthInfo.loginInfo.providerKey,
    socialAuthInfo.authInfo.accessToken,
    socialAuthInfo.authInfo.tokenType,
    socialAuthInfo.authInfo.expiresIn,
    socialAuthInfo.authInfo.refreshToken
  ))
}
