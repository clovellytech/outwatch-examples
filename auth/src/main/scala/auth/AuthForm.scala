package auth

import cats.effect.IO
import outwatch.dom._
import dsl._
import outwatch.router.dsl.C
import monix.execution.Scheduler
import monix.reactive.Observable
import outwatch.router._
import outwatch.util.Store


final case class AuthMenu()(implicit routerStore: RouterStore[Page]) {
  def render() ={
    ul(cls := "ui two item horizontal menu",
      li(cls := "item", C.a[Page]("/login")("login")),
      li(cls := "item", C.a[Page]("/register")("register"))
    )
  }
}

final case class AuthFormState(username: String, password: String)

final case class AuthPage()(implicit routerStore: RouterStore[Page], authStore: AuthStore, S: Scheduler) {
  sealed trait FormAction
  case class UpdateUsername(username: String) extends FormAction
  case class UpdatePassword(password: String) extends FormAction
  case object Init extends FormAction

  val formStore: IO[outwatch.ProHandler[FormAction, (FormAction, AuthFormState)]] =
    Store.create(Init, AuthFormState("", ""),
      Store.Reducer.justState[FormAction, AuthFormState]{
        case (state, UpdateUsername(username)) => state.copy(username = username)
        case (state, UpdatePassword(password)) => state.copy(password = password)
        case (state, _) => state
      }
    )

  def authForm(action: AuthFormState => AuthAction) = formStore.map { store =>
    div(cls := "ui form segment container",
      div(cls := "field",
        label("username"),
        input(tpe := "text",
          onInput.target.value.map(UpdateUsername) --> store
        ),
      ),
      div(cls := "field",
        label("password"),
        input(tpe := "password",
          onInput.target.value.map(UpdatePassword) --> store
        ),
      ),
      button(cls := "ui primary button",
        tpe := "button",
        store.map { case (_, state) =>
          onClick.mapTo(action(state)) --> authStore
        },
        "Go"
      )
    )
  }

  val loginForm: VDomModifier = div(
    h1("Welcome back"),
    authForm(s => AuthAction.Login(s.username, s.password))
  )

  val registerForm = div(
    h1("Welcome"),
    authForm(s => AuthAction.Register(s.username, s.password))
  )

  def render(child: VDomModifier): Observable[BasicVNode] = authStore.map { case (_, state) =>
    div(
      AuthMenu().render(),
      child,
      if (state.pending == "") div()
      else div(cls := "ui message label", state.pending)
    )
  }
}

final case class RouterComponent()(implicit store: RouterStore[Page], as: AuthStore, S: Scheduler) {
  val authPage = AuthPage()

  def render(): Observable[BasicVNode] = authPage.render(
    AppRouter.render[Page] {
      case Page.Login => authPage.loginForm
      case Page.Register => authPage.registerForm
      case Page.Profile(userId) => div(s"not implemented yet: Profile page: $userId")
      case _ => div("page content")
    }
  )
}