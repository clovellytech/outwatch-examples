package todomvc

import cats.effect.IO
import monix.execution.Scheduler
import outwatch.dom.ProHandler
import outwatch.util.Store

final case class TodoItem(id: Int, title: String, completed: Boolean)

sealed abstract class Selection(val name: String) extends Product with Serializable{
  def url: String = s"#/$name"
  def pred(t: TodoItem): Boolean
}

object Selection {
  case object All extends Selection("all"){ def pred(t: TodoItem) = true }
  case object Active extends Selection("active"){ def pred(t: TodoItem) = !t.completed }
  case object Completed extends Selection("completed"){ def pred(t: TodoItem) = t.completed }
  def selections: List[Selection] = All :: Active :: Completed :: Nil
}

sealed abstract class Action extends Product with Serializable
object Action{
  case object AddTodo extends Action
  case class UpdateText(text: String) extends Action
  case class RemoveTodo(id: Int) extends Action
  case class UpdateTodo(id: Int, newText: String) extends Action
  case class ToggleTodo(id: Int) extends Action
  case object AllComplete extends Action
  case class Drop(pred: TodoItem => Boolean) extends Action
  case class UpdateFilter(selection: Selection) extends Action
  case class EditTodo(id: Int) extends Action
  case class EditText(text: String) extends Action
  case object SaveEdit extends Action
}

final case class AppState(
  nextId: Int,
  todos: List[TodoItem],
  text: String,
  selection: Selection,
  editingId: Option[Int],
  editText: String,
)

object AppState {
  import Action._

  private def updateById(id: Int, state: AppState)(update: TodoItem => TodoItem): AppState = {
    state.todos.find(_.id == id).map{ todo =>
      state.copy(todos = update(todo) :: state.todos.filterNot(_.id == id))
    }.getOrElse(state)
  }

  def reducer(state: AppState, action: Action): AppState = action match {
    case AddTodo => state.copy(
      nextId = state.nextId + 1,
      todos = TodoItem(state.nextId, state.text, false) :: state.todos,
      text = ""
    )
    case UpdateText(text) => state.copy(text = text)
    case RemoveTodo(id) => state.copy(
      todos = state.todos.filterNot(_.id == id)
    )
    case UpdateTodo(id, newText) => updateById(id, state)(_.copy(title = newText))
    case ToggleTodo(id) => updateById(id, state)(x => x.copy(completed = !x.completed))
    case AllComplete => state.copy(todos = state.todos.map(_.copy(completed = true)))
    case Drop(pred) => state.copy(todos = state.todos.filterNot(pred))
    case UpdateFilter(selection: Selection) => state.copy(selection = selection)
    case EditTodo(id: Int) => state.copy(
      editingId = Some(id),
      editText = state.todos.find(_.id == id).map(_.title).getOrElse(""),
    )
    case EditText(text: String) => state.copy(editText = text)
    case SaveEdit => state.editingId.map(id =>
      updateById(id, state)(_.copy(title = state.editText)).copy(
        editingId = None,
        editText = ""
      )
    ).getOrElse(state)
  }

  type AppStore = ProHandler[Action, AppState]
  type SubStore[A] = ProHandler[Action, A]
  type AppReducer = Store.Reducer[AppState, Action]
  val AppReducer = Store.Reducer
  def appReducer : AppReducer = AppReducer.justState(reducer _)
  def appStore(implicit S: Scheduler): IO[AppStore] =
    Store.create(AppState(0, Nil, "", Selection.All, None, ""), appReducer)
}
