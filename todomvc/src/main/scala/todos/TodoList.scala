package todomvc

import cats.effect.IO
import outwatch.dom._
import dsl._
import monix.execution.Scheduler.Implicits.global

final case class TodoItem(id: Int, title: String, completed: Boolean)

object TodoMvcMain {

  val infoFooter: VNode = footer(
    cls := "info",
    p("Double-click to edit a todo"),
    p(a(href := "http://www.github.com/outwatch/outwatch", "Written in Scala with Outwatch")),
    p("Implements all of ",
      a(href := "http://todomvc.com/", "TodoMVC"),
      " in ",
      a(href := "https://github.com/clovellytech/outwatch-examples", "xx lines"),
    )
  )

  val todoList: IO[VNode] = for {
    text <- Handler.create[String]("")
    items <- Handler.create[List[TodoItem]](Nil)
  } yield div(cls := "todoapp",
    div(cls := "header",
      h1("todos"),
      form(
        input(
          cls := "new-todo",
          placeholder := "What needs to be done?",
          autoFocus,
          value <-- text,
          onInput.value --> text,
        ),
        text.map(x => onSubmit(items).map(TodoItem(100, x, false) :: _) --> items)
      )
    ),
    div(cls := "main",
      input(id := "toggle-all", cls := "toggle-all", tpe := "checkbox"),
      label(`for` := "toggle-all", "Mark all as complete"),
      items.map{ todos =>
        ul((cls := "todo-list") ::
          todos.map{ todo =>
            li(id := todo.id.toString,
              items.map(ts => input(tpe := "checkbox", checked := todo.completed, onChange.target.value.map(_ => todo.copy(completed = !todo.completed) :: ts.filterNot(_.id == todo.id)) --> items)),
              todo.title)
          } : _*
        )
      },
    ),
    footer(cls := "footer",
      span(cls := "todo-count", strong("fix" + " "), if (0 == 1) "item left" else "items left"),
      ul(cls := "filters",
        li(a(cls := "selected", href := "#/", "All")),
        li(a(cls := "selected", href := "#/active", "Active")),
        li(a(cls := "selected", href := "#/completed", "Completed")),
      ),
      button(cls := "clear-completed", "Clear completed"),
    ),
  )

  def todoItem(item: Handler[TodoItem]) : IO[VNode] = Handler.create[Boolean](false).map { editing =>
    li(
      div(cls := "view",
        input(cls := "toggle", tpe := "checkbox"),
        label(item.map(_.title)),
        button(cls := "destroy"),
        onDblClick(editing).map(!_) --> editing
      ),
      editing.filter(identity).map(_ => form(
        input(cls := "edit", value <-- item.map(_.title), autoFocus),
      )),
    )
  }

  def mainElement : IO[VNode] = todoList.map(div(_, infoFooter))

  def main(args: Array[String]): Unit = {
    mainElement.flatMap(OutWatch.renderReplace("#outwatch_todomvc_main", _)).unsafeRunSync()
  }
}
