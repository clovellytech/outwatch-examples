package todomvc

import AppState._
import Action._
import cats.effect.IO
import cats.implicits._
import outwatch.dom._, dsl._
import monix.execution.Scheduler

final case class TodoMvc() {
  val enter: Int = 13
  val onEnterUp = onKeyUp.transform(_.filter(_.keyCode == enter))

  def addTodo()(implicit store: AppStore): BasicVNode =
    input(cls := "new-todo",
      placeholder := "What needs to be done?",
      autoFocus,
      value <-- store.map(_._2.text),
      onInput.target.value.map(UpdateText) --> store,
      onEnterUp.mapTo(AddTodo) --> store,
    )

  def todoList()(implicit store: AppStore) = {
    store.map { case (_, state) =>
      val todos = state.todos.sortBy(-_.id).filter(state.selection.pred)
      ul((cls := "todo-list") ::
        todos.map { todo =>
          state.editingId.filter(_ == todo.id).map { _ =>
            input(cls := "edit",
              defaultValue := todo.title,
              onInput.target.value.map(EditText) --> store,
              onEnterUp.mapTo(SaveEdit) --> store,
              onBlur.mapTo(SaveEdit) --> store,
              autoFocus,
            )
          }.getOrElse {
            li(
              input(
                cls := "toggle",
                tpe := "checkbox",
                checked := todo.completed,
                onInput.map(_ => ToggleTodo(todo.id)) --> store,
              ),
              label(todo.title),
              onDblClick.mapTo(EditTodo(todo.id)) --> store,
            )
          }
        }: _*
      )
    }
  }

  private def pluralize(num: Int, singular: String, plural: String): String =
    s"$num ${if(num == 1) singular else plural}"

  def filterSelector()(implicit store : AppStore): VNode = footer(cls := "footer",
    span(cls := "todo-count",
      store.map(_._2.todos.count(!_.completed)).map(pluralize(_, "item left", "items left")),
    ),
    ul((cls := "filters") ::
      Selection.selections.map { selection =>
        li(a(
          href := selection.url,
          selection.name,
          store.map{ case (_, state) => if (state.selection == selection) cls := "selected" else cls := "" },
          onClick.map(_ => UpdateFilter(selection)) --> store,
        ))
      } : _*
    ),
    button(cls := "clear-completed", "Clear completed", onClick.map(_ => Drop(_.completed)) --> store),
  )

  def render()(implicit S: Scheduler) : IO[VNode] = appStore map { implicit store =>
    div(cls := "todoapp",
      div(cls := "header",
        h1("todos"),
        addTodo(),
        todoList(),
      ),
      div(cls := "main",
        button(tpe := "button",
          "Mark all as complete",
          onClick.map(_ => AllComplete) --> store,
        ),
      ),
      filterSelector(),
    )
  }
}
