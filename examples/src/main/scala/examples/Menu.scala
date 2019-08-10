package examples

import auth.AuthStore
import cats.effect.IO
import monix.execution.Scheduler
import outwatch.dom._
import outwatch.dom.dsl._
import outwatch.router._
import outwatch.router.dsl.C
import scala.concurrent.duration._
import todomvc.TodoMvc
import monix.reactive.Observable

sealed trait Page
case object AuthPage extends Page
case object TodoMvcPage extends Page
case object Counter extends Page
case object NotFound extends Page


object Menu {

  def router: AppRouter[Page] = AppRouter.create[Page](NotFound){
    case Root / "auth" => AuthPage
    case Root / "todomvc" => TodoMvcPage
    case Root / "counter" => Counter
    case _ => NotFound
  }

  def pageContainer()(implicit
    S: Scheduler,
    authStore: AuthStore,
    authRouter: RouterStore[auth.Page],
    router: RouterStore[Page]
  ): IO[Observable[VDomModifier]] =
    TodoMvc().render().map { todo =>
      AppRouter.render[Page] {
        case AuthPage => auth.RouterComponent().render()
        case TodoMvcPage => div(id := "todomvc", todo)
        case Counter => Observable.interval(1.second).map(count => div("Count: ", count))
        case NotFound => div()
      }
    }

  def render()(implicit scheduler: Scheduler, authStore: AuthStore, authRouter: RouterStore[auth.Page], router: RouterStore[Page]): IO[VDomModifier] =
    pageContainer().map{ pc =>
      div(
        cls := "ui two column grid",
        div(
          cls := "four wide column",
          ul(
            li(C.a[Page]("/auth")("Auth")),
            li(C.a[Page]("/counter")("Counter")),
            li(C.a[Page]("/todomvc")("TodoMvc")),
          ),
        ),
        div(
          cls := "twelve wide fluid column",
          pc,
        )
      )
    }
}
