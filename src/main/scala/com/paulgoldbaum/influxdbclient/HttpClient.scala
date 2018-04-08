package com.paulgoldbaum.influxdbclient

import java.nio.charset.Charset
import org.asynchttpclient._
import org.asynchttpclient.Realm.{AuthScheme, Builder}
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.collection.JavaConverters._

protected class HttpClient(val host: String,
                          val port: Int,
                          val https: Boolean = false,
                          val username: String = null,
                          val password: String = null,
                          val clientConfig: HttpConfig = null)(implicit ec: ExecutionContext)
{

  private val authenticationRealm = makeAuthenticationRealm()
  private var connectionClosed = false

  private val client: AsyncHttpClient = if (clientConfig == null)
    new DefaultAsyncHttpClient()
  else
    new DefaultAsyncHttpClient(clientConfig.build())

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
      .setCharset(Charset.forName("UTF-8"))
    requestBuilder.setQueryParams(params.map(p => new Param(p._1, p._2)).toList.asJava)

    makeRequest(requestBuilder)
  }

  private def makeRequest(requestBuilder: BoundRequestBuilder): Future[HttpResponse] = {
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

  private def makeAuthenticationRealm(): Realm = username match {
    case null => null
    case _ => new Builder(username, password)
      .setUsePreemptiveAuth(true)
      .setScheme(AuthScheme.BASIC)
      .build()
  }

  private class ResponseHandler(promise: Promise[HttpResponse]) extends AsyncCompletionHandler[Response] {

    override def onCompleted(response: Response): Response = {
      if (response.getStatusCode >= 400)
        promise.failure(new HttpException(
          "Server answered with error code " + response.getStatusCode + ". Details: " + response.getResponseBody,
          response.getStatusCode))
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
