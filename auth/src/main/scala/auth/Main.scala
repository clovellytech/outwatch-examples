package auth

import cats.effect.IO
import monix.execution.Scheduler.Implicits.global
import outwatch.router.RouterStore
import outwatch.dom.dsl._

object Main {

  val program: IO[Unit] = for {
    implicit0(as: AuthStore) <- AuthStore.store()
    router = AuthRouter.router()
    implicit0(routerStore: RouterStore[Page]) <- router.store
    prog <- outwatch.dom.OutWatch.renderInto("#app", div(RouterComponent().render()))
  } yield prog

  def main(args: Array[String]): Unit = program.unsafeRunSync()
}
