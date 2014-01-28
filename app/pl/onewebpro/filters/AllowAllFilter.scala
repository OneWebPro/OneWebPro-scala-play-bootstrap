package pl.onewebpro.filters

import play.api.mvc.{SimpleResult, RequestHeader, Filter}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author loki
 */
object AllowAllFilter {
	def apply(next: (RequestHeader) => Future[SimpleResult])(rh: RequestHeader) = {
		next(rh).map(_.withHeaders("Access-Control-Allow-Origin" -> "*"))
	}
}
