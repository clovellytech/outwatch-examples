package auth


final case class Profile(
  userId: String,
  username: String
)

final case class AuthState(
  userId: Option[String],
  profile: Option[Profile],
  pending: String
)

sealed trait AuthAction
object AuthAction{
  final case class Login(username: String, password: String) extends AuthAction
  final case class Register(username: String, password: String) extends AuthAction
  final case class ViewProfile(userId: String) extends AuthAction
  final case class AuthResponse(userId: String, username: String) extends AuthAction
  case object Init extends AuthAction
}

