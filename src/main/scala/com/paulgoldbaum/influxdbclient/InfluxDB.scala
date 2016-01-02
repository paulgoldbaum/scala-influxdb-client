package com.paulgoldbaum.influxdbclient

import com.paulgoldbaum.influxdbclient.Parameter.Precision.Precision

import scala.concurrent.{Future, ExecutionContext}

object InfluxDB {

  def connect(host: String = "localhost",
              port: Int = 8086,
              username: String = null,
              password: String = null,
              https: Boolean = false,
              httpConfig: HttpConfig = null): InfluxDB =
  {
     val httpClient = new HttpClient(host, port, https, username, password, httpConfig)
     new InfluxDB(httpClient)
  }

  def udpConnect(host: String, port: Int) = {
    new UdpClient(host, port)
  }
}

class InfluxDB protected[influxdbclient](httpClient: HttpClient) extends Object with UserManagement {

  implicit val ec = ExecutionContext.global

  def selectDatabase(databaseName: String) =
    new Database(databaseName, httpClient)

  def showDatabases(): Future[Seq[String]] =
    query("SHOW DATABASES")
      .map(response => response.series.head.points("name").asInstanceOf[List[String]])

  def query(query: String, precision: Precision = null): Future[QueryResult] =
    executeQuery(query, precision)
      .map(response => QueryResult.fromJson(response.content))

  def multiQuery(query: Seq[String], precision: Precision = null): Future[List[QueryResult]] =
    executeQuery(query.mkString(";"), precision)
      .map(response => QueryResult.fromJsonMulti(response.content))

  private def executeQuery(query: String, precision: Precision = null) =
    httpClient.get("/query", buildQueryParameters(query, precision))
      .recover { case error: HttpException => throw new QueryException("Error during query", error)}

  def ping() =
    httpClient.get("/ping")
      .map(response => new QueryResult())
      .recover { case error: HttpException => throw new PingException("Error during ping", error)}

  protected def buildQueryParameters(query: String, precision: Precision) = {
    val params = Map("q" -> query)
    if (precision != null)
      params + ("precision" -> precision.toString)
    else
      params
  }

  protected[influxdbclient] def getHttpClient = httpClient
}

class InfluxDBException protected[influxdbclient](str: String, throwable: Throwable) extends Exception(str, throwable)
class QueryException protected[influxdbclient](str: String, throwable: Throwable) extends InfluxDBException(str, throwable)
class PingException protected[influxdbclient](str: String, throwable: Throwable) extends InfluxDBException(str, throwable)
