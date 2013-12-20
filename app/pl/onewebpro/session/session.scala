package pl.onewebpro.session

import play.api.mvc.{RequestHeader, AnyContent, Request, Session}
import play.api.Routes

/**
 * @author loki
 */

/**
 * Session interface to create better session management
 */
trait SessionInterface {
	/**
	 * Add value to session
	 * @param value (String,T)
	 * @tparam T Object
	 * @return Session
	 */
	def :+[T](value: (String, T), context: Boolean = true)(implicit ct: T => String): Session

	/**
	 * Replace value in session
	 * @param value (String,T)
	 * @tparam T Object
	 * @return Session
	 */
	def :<-[T](value: (String, T), context: Boolean = true)(implicit ct: T => String): Session

	/**
	 * Get value from session
	 * @param name String value key
	 * @tparam T Object
	 * @return Option[T]
	 */
	def :->[T](name: String, context: Boolean = true)(implicit ct: String => T): Option[T]

	/**
	 * Get value from session removing option
	 * @param name String value key
	 * @tparam T Object
	 * @return T
	 */
	def :->>[T](name: String, context: Boolean = true)(implicit ct: String => T): T

	/**
	 * Remove value from session
	 * @param name String value key
	 * @return Session
	 */
	def :-(name: String, context: Boolean = true): Session
}

case class WrappedSession(session: Session)(implicit ctx: RequestHeader) extends SessionInterface {

	private[WrappedSession] final val DEFAULT_CONTEXT: String = "-clthy"

	private[WrappedSession] def context(withContext: Boolean): String = {
		if (withContext)
			"-" + wrapController(getController) + DEFAULT_CONTEXT
		else
			DEFAULT_CONTEXT
	}

	/**
	 * Controller name from request
	 */
	val getController: Option[String] = ctx.tags.get(Routes.ROUTE_CONTROLLER)

	/**
	 * Action name from request
	 */
	val getAction: Option[String] = ctx.tags.get(Routes.ROUTE_ACTION_METHOD)

	/**
	 * Method removes all package values form controller name
	 * @param controller Option[String]
	 * @return String
	 */
	def wrapController(controller: Option[String]): String = {
		if (controller.isEmpty)
			""
		else {
			controller.get.split("\\.").last
		}
	}

	/**
	 * Method check if we are at the @controller and @action now.
	 * @param controller String
	 * @param action String
	 * @return Boolean
	 */
	def isActive(controller: String, action: String): Boolean = Some(controller) == getController && action == getAction.getOrElse("")

	/**
	 * Add value to session
	 * @param value (String,T)
	 * @tparam T Object
	 * @return Session
	 */
	def :+[T](value: (String, T), context: Boolean = true)(implicit ct: T => String): Session = {
		session.+(value._1 + this.context(context) -> value._2)
	}

	/**
	 * For options values
	 * @param params (String,Option[T])
	 * @tparam T Object
	 * @return Session
	 */
	def ::+[T](params: (String, Option[T]), context: Boolean = true)(implicit ct: T => String): Session = {
		if (params._2.isDefined) {
			session.+(params._1 + this.context(context) -> params._2.get)
		} else {
			session
		}
	}

	/**
	 * Replace value in session
	 * @param value (String,T)
	 * @tparam T Object
	 * @return Session
	 */
	def :<-[T](value: (String, T), context: Boolean = true)(implicit ct: T => String): Session = {
		var s = session
		session.get(value._1) match {
			case None => s = this.:+[T](value._1 -> value._2, context)
			case _ => {
				this.:-(value._1, context)
				s = this.:+[T](value._1 -> value._2, context)
			}
		}
		s
	}

	/**
	 * Get value from session
	 * @param name String value key
	 * @tparam T Object
	 * @return Option[T]
	 */
	def :->[T](name: String, context: Boolean = true)(implicit ct: String => T): Option[T] = {
		session.get(name + this.context(context)).map[T]((p) => p)
	}

	/**
	 * Get value from session removing option
	 * @param name String value key
	 * @tparam T Object
	 * @return T
	 */
	def :->>[T](name: String, context: Boolean = true)(implicit ct: String => T): T = {
		session.get(name + this.context(context)).map[T]((p) => p).get
	}

	/**
	 * Remove value from session
	 * @param name String value key
	 * @return Session
	 */
	def :-(name: String, context: Boolean = true): Session = {
		session - (name + this.context(context))
	}
}


/**
 * Session wrapper to add implicit wrap a session and extend
 */
case object SessionWrapper {

	implicit def session(ws: WrappedSession): Session = ws.session

	implicit def ws(session: Session)(implicit ctx: RequestHeader): WrappedSession = WrappedSession(session)(ctx)

	implicit def s(implicit ctx: RequestHeader): WrappedSession = WrappedSession(ctx.session)(ctx)
}
