package com.paulgoldbaum.influxdbclient

import com.paulgoldbaum.influxdbclient.HttpClient.HttpResponse

import scala.concurrent.Future

object Mocks {
  class ExceptionThrowingHttpClient(host: String, port: Int) extends HttpClient(host, port) {
    override def post(url: String, params: Map[String, String] = Map(), content: String): Future[HttpResponse] =
      Future.failed(new HttpException(""))

    override def get(url: String, params: Map[String, String] = Map()): Future[HttpResponse] =
      Future.failed(new HttpException(""))
  }

  class ErrorReturningHttpClient(host: String, port: Int, errorCode: Int) extends HttpClient(host, port) {
    override def post(url: String, params: Map[String, String] = Map(), content: String): Future[HttpResponse] =
      Future.successful(new HttpResponse(errorCode, "Error message"))

    override def get(url: String, params: Map[String, String] = Map()): Future[HttpResponse] =
      Future.successful(new HttpResponse(errorCode, "Error message"))
  }

}
