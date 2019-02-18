import outwatch.dom.ProHandler

package object auth {
  type AuthStore = ProHandler[AuthAction, (AuthAction, AuthState)]
}
