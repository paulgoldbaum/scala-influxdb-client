package com.paulgoldbaum.influxdbclient

import com.ning.http.client.{Response, AsyncCompletionHandler, AsyncHttpClient}
import scala.concurrent.{ExecutionContext, Future, Promise}

class HttpClient(host: String, port: Int, username: String = null, password: String = null) {

  implicit val ec = ExecutionContext.global

  def get(url: String, params: Map[String, String] = Map()): Future[HttpResponse] = {
    val client = new AsyncHttpClient()
    val resultPromise = Promise[HttpResponse]()
    var requestBuilder = client.prepareGet(s"http://$host:$port/$url")

    if (username != null)
      requestBuilder = requestBuilder.addQueryParam("username", username)

    if (password != null)
      requestBuilder = requestBuilder.addQueryParam("password", password)

    params.foreach(param => requestBuilder = requestBuilder.addQueryParam(param._1, param._2))

    requestBuilder.execute(new AsyncCompletionHandler[Response] {
      override def onCompleted(response: Response): Response = {
        resultPromise.success(HttpResponse(response.getStatusCode, response.getResponseBody))
        response
      }
    })

    resultPromise.future
  }

  case class HttpResponse(code: Int, content: String)
  case class HttpJsonResponse(code: Int, content: Map[String, Object])
}
