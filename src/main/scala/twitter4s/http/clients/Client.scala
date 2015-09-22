package twitter4s.http.clients

import scala.concurrent.Future

import spray.client.pipelining._
import spray.http.{HttpRequest, HttpResponse}
import spray.httpx.unmarshalling.{FromMessageUnmarshaller, FromResponseUnmarshaller, UnmarshallerLifting}
import twitter4s.http.unmarshalling.JsonSupport
import twitter4s.providers.ActorRefFactoryProvider
import twitter4s.utils.ActorContextExtractor

trait Client extends JsonSupport with ActorContextExtractor with UnmarshallerLifting {
  self: ActorRefFactoryProvider =>

  def sendReceiveAs[T](request: HttpRequest)(implicit mu: FromMessageUnmarshaller[T]) = {
    val ru: FromResponseUnmarshaller[T] = fromResponseUnmarshaller(mu)
    pipeline(ru) apply request
  }

  implicit def toResponse[T: Manifest](request: HttpRequest): Future[T] = sendReceiveAs[T](request)

  // TODO - logRequest, logResponse customisable?
  // TODO - link request and response?
  def pipeline[T: FromResponseUnmarshaller] =
    logRequest ~> sendReceive ~> logResponse ~> unmarshal[T]

  def sendReceive = spray.client.pipelining.sendReceive

  def logRequest: HttpRequest => HttpRequest = { request =>
    log.info(s"${request.method} ${request.uri}")
    request
  }

  // TODO - improve response logging
  def logResponse: Future[HttpResponse] => Future[HttpResponse] = { futureResponse =>
    futureResponse.map { response =>
      response.status.isSuccess match {
        case true => log.info(s"${response.status} ${response.headers}")
        case false => log.error(response.toString)
      }
    }
    futureResponse
  }

}