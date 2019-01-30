package todomvc

import outwatch.dom.OutWatch
import monix.execution.Scheduler.Implicits.global

object TodoMvcMain{
  def main(args: Array[String]): Unit = {
    TodoMvc().render().flatMap(OutWatch.renderInto("#todoapp", _)).unsafeRunSync()
  }
}
