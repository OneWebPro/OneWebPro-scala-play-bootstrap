package pl.onewebpro.controllers

import org.joda.time.DateTime
import concurrent.ExecutionContext
import akka.util.Timeout
import concurrent.duration.`package`._
import play.api.mvc._
import play.libs.Akka
import akka.actor.ActorRef
import pl.onewebpro.secure.{SecureService, Secure}

/**
 * @author loki
 */
/**
 * Global controller for secured controllers
 */
trait GlobalController extends Controller with Secure {

	/**
	 * Global database actor
	 */
	val globalActor: ActorRef

	/**
	 * Time
	 */
	implicit val time = () => new DateTime()

	/**
	 * Actor context
	 */
	implicit val executionContext: ExecutionContext = Akka.system.dispatcher

	/**
	 * Actor timeout for request
	 */
	implicit val timeout: Timeout = 10 seconds

	/**
	 * Security service for menu
	 */
	implicit val securityService: SecureService = SecureService

}


