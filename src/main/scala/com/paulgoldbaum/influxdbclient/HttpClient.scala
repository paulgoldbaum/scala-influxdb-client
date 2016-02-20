package com.paulgoldbaum.influxdbclient

import com.ning.http.client.{Response, AsyncCompletionHandler, Param, AsyncHttpClient}
import com.ning.http.client.Realm.{AuthScheme, RealmBuilder}
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.collection.JavaConverters._

protected class HttpClient(val host: String,
                          val port: Int,
                          val https: Boolean = false,
                          val username: String = null,
                          val password: String = null,
                          val clientConfig: HttpConfig = null)
{

  implicit private val ec = ExecutionContext.global
  private val authenticationRealm = makeAuthenticationRealm()
  private var connectionClosed = false

  private val client: AsyncHttpClient = if (clientConfig == null)
    new AsyncHttpClient()
  else
    new AsyncHttpClient(clientConfig.build())

  private val protocol = if (https) "https" else "http"

  def get(url: String, params: Map[String, String] = Map()): Future[HttpResponse] = {
    val requestBuilder = client.prepareGet("%s://%s:%d%s".format(protocol, host, port, url))
      .setRealm(authenticationRealm)
    requestBuilder.setQueryParams(params.map(p => new Param(p._1, p._2)).toList.asJava)

    makeRequest(requestBuilder)
  }

  def post(url: String, params: Map[String, String] = Map(), content: String): Future[HttpResponse] = {
    val requestBuilder = client.preparePost("%s://%s:%d%s".format(protocol, host, port, url))
      .setRealm(authenticationRealm)
      .setBody(content)
    requestBuilder.setQueryParams(params.map(p => new Param(p._1, p._2)).toList.asJava)

    makeRequest(requestBuilder)
  }

  private def makeRequest(requestBuilder: AsyncHttpClient#BoundRequestBuilder): Future[HttpResponse] = {
    val resultPromise = Promise[HttpResponse]()
    if (isClosed)
      return resultPromise.failure(new HttpException("Connection is already closed")).future

    requestBuilder.execute(new ResponseHandler(resultPromise))
    resultPromise.future
  }

  def close() = {
    if (isClosed)
      throw new HttpException("Connection is already closed")

    client.close()
    connectionClosed = true
  }

  def isClosed = connectionClosed

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
        promise.failure(new HttpException("Server answered with error code " + response.getStatusCode, response.getStatusCode))
      else
        promise.success(new HttpResponse(response.getStatusCode, response.getResponseBody))
      response
    }

    override def onThrowable(throwable: Throwable) = {
      promise.failure(new HttpException("An error occurred during the request", -1, throwable))
    }
  }

}

class HttpException protected[influxdbclient]
(val str: String, val code: Int = -1, val throwable: Throwable = null) extends Exception(str, throwable) {}

case class HttpResponse(code: Int, content: String)

case class HttpJsonResponse(code: Int, content: Map[String, Object])
