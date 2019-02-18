package auth

import outwatch.util.Store
import monix.execution.Scheduler
import monix.reactive.Observable
import AuthAction._
import cats.effect.IO

object AuthStore {
  def reducer(state: AuthState, action: AuthAction): (AuthState, Observable[AuthAction]) = action match {
    case a: Login =>
      (state.copy(pending = "Sent " + a.toString), Observable.empty)

    case a: Register =>
      (state.copy(pending = "Sent " + a.toString), Observable.empty)

    case ViewProfile(_) =>
      (state, Observable.empty)

    case AuthResponse(userId, username) =>
      (state.copy(profile = Some(Profile(userId, username))), Observable.empty)

    case _ =>
      (state, Observable.empty)
  }

  def store()(implicit S: Scheduler): IO[AuthStore] = Store.create(
    Init,
    AuthState(None, None, ""),
    Store.Reducer.stateAndEffects(reducer _)
  )
}
