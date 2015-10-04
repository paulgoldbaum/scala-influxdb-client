package com.paulgoldbaum.influxdbclient

import com.ning.http.client.Realm.{AuthScheme, RealmBuilder}
import com.ning.http.client.{Response, AsyncCompletionHandler, AsyncHttpClient}
import scala.concurrent.{ExecutionContext, Future, Promise}

class HttpClient(host: String, port: Int, username: String = null, password: String = null) {

  implicit val ec = ExecutionContext.global
  val client = new AsyncHttpClient()
  val authenticationRealm = makeAuthenticationRealm()

  def get(url: String, params: Map[String, String] = Map()): Future[HttpResponse] = {
    var requestBuilder = client.prepareGet("http://%s:%d/%s".format(host, port, url))
      .setRealm(authenticationRealm)
    params.foreach(param => requestBuilder = requestBuilder.addQueryParam(param._1, param._2))

    val resultPromise = Promise[HttpResponse]()
    requestBuilder.execute(new ResponseHandler(resultPromise))
    resultPromise.future
  }

  def post(url: String, params: Map[String, String] = Map(), content: String): Future[HttpResponse] = {
    var requestBuilder = client.preparePost("http://%s:%d/%s".format(host, port, url))
      .setRealm(authenticationRealm)
      .setBody(content)
    params.foreach(param => requestBuilder = requestBuilder.addQueryParam(param._1, param._2))

    val resultPromise = Promise[HttpResponse]()
    requestBuilder.execute(new ResponseHandler(resultPromise))
    resultPromise.future
  }

  private def makeAuthenticationRealm() = username match {
    case null => null
    case _ => new RealmBuilder()
      .setPrincipal(username)
      .setPassword(password)
      .setUsePreemptiveAuth(true)
      .setScheme(AuthScheme.BASIC)
      .build()
  }

  private class ResponseHandler(promise: Promise[HttpResponse]) extends AsyncCompletionHandler[Response] {

    override def onCompleted(response: Response): Response = {
      if (response.getStatusCode >= 400)
        promise.failure(new HttpException("Server answered with error code " + response.getStatusCode))
      else
        promise.success(HttpResponse(response.getStatusCode, response.getResponseBody))
      response
    }
  }

  case class HttpResponse(code: Int, content: String)

  case class HttpJsonResponse(code: Int, content: Map[String, Object])

  class HttpException(str: String) extends Exception(str)

}