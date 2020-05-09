package utils.filters

import akka.stream.Materializer
import com.google.inject.Inject
import net.ceedubs.ficus.Ficus._
import play.api.Configuration
import play.api.mvc.{ Filter, RequestHeader, Result, Results }

import scala.concurrent.{ ExecutionContext, Future }

class ApiKeyFilter @Inject() (config: Configuration)(implicit val mat: Materializer, ec: ExecutionContext)
  extends Filter {
  override def apply(f: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] = {
    rh.headers.get("X-ApiKey") match {
      case None => Future.successful(Results.Forbidden)
      case Some(apiKey) => {
        if (apiKey == config.underlying.as[String]("api-key")) f(rh)
        else Future.successful(Results.Forbidden)
      }
    }
  }
}
