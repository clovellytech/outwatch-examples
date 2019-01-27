package todomvc

import outwatch.dom.OutWatch
import monix.execution.Scheduler.Implicits.global

object TodoMvcMain{
  def main(args: Array[String]): Unit = {
    TodoMvc().flatMap(OutWatch.renderReplace("#outwatch_todomvc_main", _)).unsafeRunSync()
  }
}
