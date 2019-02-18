package examples

import auth.AuthStore
import cats.effect.IO
import outwatch.dom._
import outwatch.dom.dsl._
import monix.execution.Scheduler.Implicits.global
import outwatch.router._

object Main {
  def program: IO[Unit] = for {
    implicit0(authStore: AuthStore) <- auth.AuthStore.store()
    implicit0(exRouterStore: RouterStore[examples.Page]) <- Menu.router.store
    implicit0(authRouterStore: RouterStore[auth.Page]) <- auth.AuthRouter.router(Root / "auth").store
    program <- OutWatch.renderInto("#app", div(Menu.render()))
  } yield program

  def main(args: Array[String]): Unit = program.unsafeRunSync()
}
