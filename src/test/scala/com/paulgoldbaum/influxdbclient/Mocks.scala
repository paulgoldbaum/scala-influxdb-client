package com.paulgoldbaum.influxdbclient

import scala.concurrent.{ExecutionContext, Future}

object Mocks {
  class ExceptionThrowingHttpClient(host: String, port: Int)(implicit ec: ExecutionContext)
      extends HttpClient(host, port) {
    override def post(url: String, params: Map[String, String] = Map(), content: String): Future[HttpResponse] =
      Future.failed(new HttpException(""))

    override def get(url: String, params: Map[String, String] = Map()): Future[HttpResponse] =
      Future.failed(new HttpException(""))
  }

  class ErrorReturningHttpClient(host: String, port: Int, errorCode: Int)(implicit ec: ExecutionContext)
      extends HttpClient(host, port) {
    override def post(url: String, params: Map[String, String] = Map(), content: String): Future[HttpResponse] =
      Future.successful(new HttpResponse(errorCode, "Error message"))

    override def get(url: String, params: Map[String, String] = Map()): Future[HttpResponse] =
      Future.successful(new HttpResponse(errorCode, "Error message"))
  }

}
