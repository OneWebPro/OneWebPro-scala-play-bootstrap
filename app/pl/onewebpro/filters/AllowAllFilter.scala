package pl.onewebpro.filters

import play.api.mvc.{SimpleResult, RequestHeader, Filter}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author loki
 */
object AllowAllFilter extends Filter{
	def apply(next: (RequestHeader) => Future[SimpleResult])(rh: RequestHeader) = {
		next(rh).map(_.withHeaders(
			"Access-Control-Allow-Origin" -> rh.headers.get("Origin").getOrElse(""),
			"Access-Control-Allow-Credentials" -> "true"
		))
	}
}
