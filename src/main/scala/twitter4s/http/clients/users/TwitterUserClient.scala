package twitter4s.http.clients.users

import scala.concurrent.Future

import twitter4s.entities.{Banners, User}
import twitter4s.http.clients.OAuthClient
import twitter4s.http.clients.users.parameters.{BannersParameters, UserParameters, UsersParameters}
import twitter4s.util.Configurations

trait TwitterUserClient extends OAuthClient with Configurations {

  val usersUrl = s"$apiTwitterUrl/$twitterVersion/users"

  def users(screen_names: String*): Future[Seq[User]] = users(screen_names)

  def users(screen_names: Seq[String],
            include_entities: Boolean = true): Future[Seq[User]] = {
    require(!screen_names.isEmpty, "please, provide at least one screen name")
    val parameters = UsersParameters(user_id = None, Some(screen_names.mkString(",")), include_entities)
    genericUsers(parameters)
  }

  def usersByIds(ids: Long*): Future[Seq[User]] = usersByIds(ids)

  def usersByIds(ids: Seq[Long],
                    include_entities: Boolean = true): Future[Seq[User]] = {
    require(!ids.isEmpty, "please, provide at least one user id")
    val parameters = UsersParameters(Some(ids.mkString(",")), screen_name = None, include_entities)
    genericUsers(parameters)
  }

  private def genericUsers(parameters: UsersParameters): Future[Seq[User]] =
    Get(s"$usersUrl/lookup.json", parameters).respondAs[Seq[User]]

  def user(screen_name: String, include_entities: Boolean = true): Future[User] = {
    val parameters = UserParameters(user_id = None, Some(screen_name), include_entities)
    genericUser(parameters)
  }

  def userById(id: Long, include_entities: Boolean = true): Future[User] = {
    val parameters = UserParameters(Some(id), screen_name = None, include_entities)
    genericUser(parameters)
  }

  private def genericUser(parameters: UserParameters): Future[User] =
    Get(s"$usersUrl/show.json", parameters).respondAs[User]

  // TODO - search a user

  def profileBanners(screen_name: String): Future[Banners] = {
  val parameters = BannersParameters(user_id = None, Some(screen_name))
    genericProfileBanners(parameters)
  }

  def profileBannersPerUserId(user_id: Long): Future[Banners] = {
    val parameters = BannersParameters(Some(user_id), screen_name = None)
    genericProfileBanners(parameters)
  }

  private def genericProfileBanners(parameters: BannersParameters): Future[Banners] =
    Get(s"$usersUrl/profile_banner.json", parameters).respondAs[Banners]
}
