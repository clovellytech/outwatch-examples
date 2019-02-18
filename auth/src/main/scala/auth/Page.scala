package auth

import outwatch.router._

sealed trait Page
object Page{
  case object Home extends Page
  case object Login extends Page
  case object Register extends Page
  final case class Profile(userId: String) extends Page
}


object AuthRouter {

  def router(): AppRouter[Page] = router(Root)

  def router(Base: Path): AppRouter[Page] = AppRouter.create[Page](Base, Page.Home) {
    case Root => Page.Home
    case Root / "login" => Page.Login
    case Root / "register" => Page.Register
    case Root / "profile" / userId => Page.Profile(userId)
    case _ => Page.Home
  }
}
