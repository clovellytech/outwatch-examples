package todomvc

import cats.effect.IO
import cats.implicits._
import outwatch.dom._
import dsl._
import monix.reactive.Observable

final case class TodoItem(id: Int, title: String, completed: Boolean)

sealed abstract class Selection(val name: String) extends Product with Serializable{
  def url: String = s"#/$name"
}

object Selection{
  case object All extends Selection("all")
  case object Active extends Selection("active")
  case object Completed extends Selection("completed")
  def selections: List[Selection] = All :: Active :: Completed :: Nil
}

object TodoMvc {
  val enter: Int = 13
  val onEnterUp = onKeyUp.transform(_.filter(_.keyCode == enter))

  def replaceTodo(id: Int, todo: TodoItem, todos: List[TodoItem]): List[TodoItem] =
    (todo :: todos.filterNot(_.id == id)).sortBy(_.id)

  def addTodo(todoHandler: Handler[(Int, List[TodoItem])]): IO[VNode] = Handler.create[String]("").map { text =>
    input(cls := "new-todo",
      placeholder := "What needs to be done?",
      autoFocus,
      value <-- text,
      onChange.target.value --> text,
      text zip todoHandler map { case (title, (nextId, todos)) =>
        onEnterUp.map(_ => (nextId + 1, TodoItem(nextId, title, false) :: todos)) --> todoHandler
      }
    )
  }

  def todoItem(item: Handler[TodoItem]): IO[VNode] = Handler.create[Boolean](false) map { editing =>
    val titleHandler = item.transformHandler[String](_.flatMap(x => item.map(_.copy(title = x))))(_.map(_.title))
    li(
      editing.map {
        case false =>
          div(cls := "view",
            input(cls := "toggle", tpe := "checkbox"),
            titleHandler.map(label(_)),
            onDblClick.mapTo(true) --> editing
          )
        case true =>
          div(cls := "edit",
            input(
              autoFocus,
              value <-- titleHandler,
              onInput.target.value --> titleHandler,
              onEnterUp.mapTo(false) --> editing,
              onBlur.mapTo(false) --> editing
            ),
          )
      },
    )
  }

  def todoList(todosHandler: Handler[(Int, List[TodoItem])]): VDomModifier = ul(cls := "todo-list",
    todosHandler.map{ case (_, todos) =>
      todos.map{ case todo =>
        val itemWriter : Observable[TodoItem] => Observable[(Int, List[TodoItem])] =
          _.flatMap(newItem => todosHandler.map{ case (id, todos) => (id, replaceTodo(todo.id, newItem, todos))})
        val itemReader : Observable[(Int, List[TodoItem])] => Observable[TodoItem] =
          _.map(_ => todo)  // probably wrong...
        val itemHandler = todosHandler.transformHandler[TodoItem](itemWriter)(itemReader)
        todoItem(itemHandler)
      }
    }
  )

  def filterSelector(selected: Handler[Selection]): VDomModifier = footer(cls := "footer",
    ul((cls := "filters") ::
      Selection.selections.map { selection =>
        li(a(selected.filter(_.name == selection.name).as(cls := "selected"), href := selection.url, selection.name, onClick.mapTo(selection) --> selected))
      } : _*
    ),
    button(cls := "clear-completed", "Clear completed"),
  )

  val infoFooter: VNode = footer(
    cls := "info",
    p("Double-click to edit a todo"),
    p(a(href := "http://www.github.com/outwatch/outwatch", "Written in Scala with Outwatch")),
    p("Implements", a(href := "http://todomvc.com/", "TodoMVC")),
  )

  def apply() : IO[VNode] = for {
    items <- Handler.create[(Int, List[TodoItem])]((0, Nil))
    selected <- Handler.create[Selection](Selection.All)
  } yield div(cls := "todoapp",
    div(cls := "header",
      h1("todos"),
      addTodo(items),
      todoList(items)
    ),
    div(cls := "main",
      button(tpe := "button",
        items.map{ case (id, todos) => onClick.map(_ => (id, todos.map(_.copy(completed = true)))) --> items},
        "Mark all as complete"
      )
    ),
    items.map { case (_, todos) =>
      val incomplete = todos.count(!_.completed)
      span(cls := "todo-count", incomplete, if (incomplete == 1) " item left" else " items left")
    },
    filterSelector(selected),
    infoFooter,
  )
}
