
package pl.onewebpro.secure

import play.api.mvc._
import scala.concurrent.Future
import pl.onewebpro.database.ServiceError
import pl.onewebpro.secure

/**
 * Constants for secure
 */
object SecureConstants extends Enumeration {
	type SecureConstants = Value
	val Token = Value("XSRF-TOKEN")
}

/**
 * @author loki
 */

trait SecureParams {
	/**
	 * Redirect to index and show flash message
	 * @param request play.api.mvc.RequestHeader
	 * @return
	 */
	def onUnauthorized(request: RequestHeader): SimpleResult

	/**
	 * Redirect to index and show flash message
	 * @param request play.api.mvc.RequestHeader
	 * @return
	 */
	def notOnUnauthorized(request: RequestHeader): SimpleResult

	/**
	 * Redirect to index and show flash message
	 * @param request play.api.mvc.RequestHeader
	 * @return
	 */
	def notPermissions(request: RequestHeader): SimpleResult

	/**
	 * Bad request action
	 * @param request play.api.mvc.RequestHeader
	 * @return
	 */
	def badRequest(request: RequestHeader): SimpleResult

}

trait SecureInfo {
	/**
	 * Application key stored in session
	 */
	val key: String = "key"

	/**
	 * Check in session if security key in cookie
	 * @param request play.api.mvc.RequestHeader
	 * @return
	 */
	def userInfo(request: RequestHeader): Option[String] = request.session.get(MD5.hash(key))

	/**
	 * Add user to session
	 * @param request play.api.mvc.RequestHeader
	 * @param value String
	 * @return
	 */
	def userLogin(value: String)(implicit request: RequestHeader): Session = request.session.+(MD5.hash(key), value)
}

/**
 * Trait to extend controllers to give them secure check
 */
trait Secure extends SecureParams with SecureInfo {

	/**
	 * User type for user requests
	 */
	type UserType

	/**
	 * User request to get user from database or cache
	 * @return
	 */
	def getUserInfo: (RequestHeader) => Either[ServiceError, UserType]

	/**
	 * Implicit cheat for variables
	 */
	implicit val secure: Secure = this

	/**
	 * Authorized action
	 */
	lazy val Auth: Auth = new Auth

	/**
	 * Authorized action with user from cache
	 */
	lazy val AuthUser: AuthUser[UserType] = new AuthUser(getUserInfo)

	/**
	 * Authorized action for ajax use
	 */
	lazy val AuthAjax: AuthAjax = new AuthAjax

	/**
	 * Authorized action for ajax use with user
	 */
	lazy val AuthAjaxUser: AuthAjaxUser[UserType] = new AuthAjaxUser(getUserInfo)

	/**
	 * Not authorized action
	 */
	lazy val NotAuth: NotAuth = new NotAuth

	/**
	 * Not authorized action for ajax use
	 */
	lazy val NotAuthAjax: NotAuthAjax = new NotAuthAjax


	/**
	 * Check if request is ajax typed
	 * @param request play.api.mvc.RequestHeader
	 * @tparam A Request type
	 * @return Boolean
	 */
	def isAjax[A](implicit request: Request[A]) = {
		request.headers.get("X-Requested-With") == Some("XMLHttpRequest")
	}

}

trait SecureCSRF extends Secure {

	implicit val manager: CSRFManager = DefaultCSRFManager

	/**
	 * Authorized action for ajax use and check CSRF token
	 */
	lazy val AuthAjaxCSRF: AuthAjaxCSRF = new AuthAjaxCSRF

	/**
	 * Authorized action for ajax use with user and check CSRF token
	 */
	lazy val AuthCSRFAjaxUser: AuthAjaxUserCSRF[UserType] = new AuthAjaxUserCSRF(getUserInfo)


	def CSRFCookie(value: String)(implicit request: RequestHeader): Cookie = Cookie(SecureConstants.Token.toString, manager.hash(value), httpOnly = false)

}

/**
 * A request with user id
 * @param userId String
 * @param request play.api.mvc.Request
 * @tparam A Result type
 */
class AuthenticatedRequest[A](val userId: String, request: Request[A]) extends WrappedRequest[A](request)

/**
 * Request with user object
 * @param user User
 */

case class UserType(user: Any) {
	/**
	 * Get user as controller type
	 * @tparam UserType User controller type
	 * @return
	 */
	def get[UserType]: UserType = user.asInstanceOf[UserType]
}

class UserRequest[A](val user: UserType, request: Request[A]) extends WrappedRequest[A](request)

/**
 * Ajax compose action. It check if action was request by ajax request. If not it will redirect to other view.
 * @param action play.api.mvc.Action
 * @tparam A Result type
 */
case class Ajax[A](action: Action[A])(implicit secure: Secure) extends Action[A] {

	def apply(request: Request[A]): Future[SimpleResult] = {
		secure.isAjax(request) match {
			case true => action(request)
			case _ => Future.successful(secure.badRequest(request))
		}
	}

	lazy val parser = action.parser
}

/**
 * Authorization action using ActionBuilder. Use controller global configuration.
 */
class Auth(implicit secure: Secure) extends ActionBuilder[AuthenticatedRequest] {
	protected def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[SimpleResult]): Future[SimpleResult] = {
		secure.userInfo(request).map(user => {
			block(new AuthenticatedRequest(user, request))
		}).getOrElse(Future.successful(secure.onUnauthorized(request)))
	}
}

class AuthUser[UserType](function: (Request[_]) => Either[ServiceError, UserType])(implicit secure: Secure) extends ActionBuilder[UserRequest] {
	protected def invokeBlock[A](request: Request[A], block: (UserRequest[A]) => Future[SimpleResult]): Future[SimpleResult] = {
		function.apply(request) match {
			case Left(ko) => Future.successful(secure.onUnauthorized(request).flashing("error" -> ko.error))
			case Right(ok) => block(new UserRequest(UserType(ok), request))
		}
	}
}

class AuthCSRF(implicit secure: Secure, manager: CSRFManager) extends Auth {
	override protected def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[SimpleResult]): Future[SimpleResult] = {
		secure.userInfo(request).map(user => {
			if (request.headers.get(SecureConstants.Token.toString) == Some(manager.hash(user))) {
				block(new AuthenticatedRequest(user, request))
			} else {
				Future.successful(secure.onUnauthorized(request))
			}
		}).getOrElse(Future.successful(secure.onUnauthorized(request)))
	}
}

class AuthUserCSRF[UserType](function: (Request[_]) => Either[ServiceError, UserType])(implicit secure: Secure, manager: CSRFManager) extends AuthUser[UserType](function) {
	override protected def invokeBlock[A](request: Request[A], block: (UserRequest[A]) => Future[SimpleResult]): Future[SimpleResult] = {
		function.apply(request) match {
			case Left(ko) => Future.successful(secure.onUnauthorized(request).flashing("error" -> ko.error))
			case Right(ok) =>
				secure.userInfo(request).map(user => {
					if (request.headers.get(SecureConstants.Token.toString) == Some(manager.hash(user))) {
						block(new UserRequest(UserType(ok), request))
					} else {
						Future.successful(secure.onUnauthorized(request))
					}
				}).getOrElse(Future.successful(secure.onUnauthorized(request)))
		}
	}
}


/**
 * The same authorization action like Auth but for ajax requests.
 */
class AuthAjax(implicit secure: Secure) extends Auth {
	override protected def composeAction[A](action: Action[A]): Action[A] = new Ajax[A](action)
}

class AuthAjaxCSRF(implicit secure: Secure, manager: CSRFManager) extends AuthCSRF {
	override protected def composeAction[A](action: Action[A]): Action[A] = new Ajax[A](action)
}

class AuthAjaxUser[UserType](function: (Request[_]) => Either[ServiceError, UserType])(implicit secure: Secure) extends AuthUser[UserType](function) {
	override protected def composeAction[A](action: Action[A]): Action[A] = new Ajax[A](action)
}

class AuthAjaxUserCSRF[UserType](function: (Request[_]) => Either[ServiceError, UserType])(implicit secure: Secure, manager: CSRFManager) extends AuthUserCSRF[UserType](function) {
	override protected def composeAction[A](action: Action[A]): Action[A] = new Ajax[A](action)
}

/**
 * Action for not authorized actions. Use controller global configuration
 */
class NotAuth(implicit secure: Secure) extends ActionBuilder[Request] {
	protected def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[SimpleResult]): Future[SimpleResult] = {
		secure.userInfo(request) match {
			case Some(_) => Future.successful(secure.notOnUnauthorized(request))
			case _ => block(request)
		}
	}
}

/**
 * The same not authorization action like NotAuth but for ajax requests.
 */
class NotAuthAjax(implicit secure: Secure) extends NotAuth {
	override protected def composeAction[A](action: Action[A]): Action[A] = new Ajax[A](action)
}

/**
 * Secure trait to check if menu is enabled
 */
trait SecureService {
	def userIsLogged()(implicit request: RequestHeader): Boolean
}

/**
 * Secure service
 */
object SecureService extends SecureService with SecureInfo {

	/**
	 * Check if user is logged
	 * @param request play.api.mvc.RequestHeader Our request
	 * @return boolean
	 */
	def userIsLogged()(implicit request: RequestHeader): Boolean = {
		userInfo(request).isDefined
	}
}

/**
 * Object to build a strong password protection.
 */
trait PasswordManager {
	def hash(password: String): String
}

/**
 * Manager for CSRF codes
 */
trait CSRFManager extends PasswordManager

/**
 * Default CSRF manager
 */
object DefaultCSRFManager extends CSRFManager {
	def hash(password: String): String = secure.MD5.hash(password)
}

/**
 * Default password manager
 */
object DefaultPasswordManager extends PasswordManager {
	def hash(password: String): String = secure.MD5.hash(password)
}

/**
 * Scala implementation od MD5
 */
object MD5 {
	def hash(s: String): String = {
		val m = java.security.MessageDigest.getInstance("MD5")
		val b = s.getBytes("UTF-8")
		m.update(b, 0, b.length)
		new java.math.BigInteger(1, m.digest()).toString(16)
	}
}
