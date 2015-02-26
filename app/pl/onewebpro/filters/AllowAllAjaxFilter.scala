package pl.onewebpro.filters

import play.api.mvc.{Result, RequestHeader, Filter}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author loki
 */
object AllowAllAjaxFilter extends Filter {
  def apply(next: (RequestHeader) => Future[Result])(rh: RequestHeader) = {
    next(rh).map(_.withHeaders(
      "Access-Control-Allow-Origin" -> rh.headers.get("Origin").getOrElse(""),
      "Access-Control-Allow-Credentials" -> "true"
    ).as("application/json; charset=utf-8"))
  }
}
