package com.paulgoldbaum.influxdbclient

import com.ning.http.client.Realm.{AuthScheme, RealmBuilder}
import com.ning.http.client.{AsyncHttpClientConfig, Response, AsyncCompletionHandler, AsyncHttpClient}
import scala.concurrent.{ExecutionContext, Future, Promise}


protected object HttpClient {

  case class HttpResponse(code: Int, content: String)

  case class HttpJsonResponse(code: Int, content: Map[String, Object])

  class Config {
    private var builder = new AsyncHttpClientConfig.Builder

    def setConnectTimeout(timeout: Int) = {
      builder = builder.setConnectTimeout(timeout)
      this
    }

    def setReadTimeout(timeout: Int) = {
      builder = builder.setReadTimeout(timeout)
      this
    }

    def setRequestTimeout(timeout: Int) = {
      builder = builder.setRequestTimeout(timeout)
      this
    }

    protected[influxdbclient] def build() = builder.build()
  }
}

protected class HttpClient(host: String,
                 port: Int,
                 username: String = null,
                 password: String = null,
                 clientConfig: HttpClient.Config = null)
{
  import HttpClient._

  implicit val ec = ExecutionContext.global
  val authenticationRealm = makeAuthenticationRealm()

  val client: AsyncHttpClient = if (clientConfig == null)
    new AsyncHttpClient()
  else
    new AsyncHttpClient(clientConfig.build())

  def get(url: String, params: Map[String, String] = Map()): Future[HttpResponse] = {

    var requestBuilder = client.prepareGet("http://%s:%d%s".format(host, port, url))
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
        promise.success(new HttpResponse(response.getStatusCode, response.getResponseBody))
      response
    }

    override def onThrowable(throwable: Throwable) = {
      promise.failure(new HttpException("An error occurred during the request", throwable))
    }
  }

}

class HttpException(str: String, throwable: Throwable = null) extends Exception(str, throwable)
