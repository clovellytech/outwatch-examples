package todomvc

import outwatch.dom._, dsl._
import monix.execution.Scheduler.Implicits.global

object TodoMvcMain{

  def mainElement = div(
    h1("HELLO OUTWATCH")
  )

  def main(args: Array[String]): Unit = {
    OutWatch.renderReplace("#outwatch_todomvc_main", mainElement).unsafeRunSync()
  }
}

