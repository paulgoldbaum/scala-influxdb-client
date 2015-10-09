package com.paulgoldbaum.influxdbclient

import scala.concurrent.{Future, ExecutionContext}

class Client protected[influxdbclient]
(httpClient: HttpClient) {

  implicit val ec = ExecutionContext.global

  def selectDatabase(databaseName: String) =
    new Database(databaseName, httpClient)

  def showDatabases(): Future[Seq[String]] = {
    query("SHOW DATABASES")
      .map(response => response.series.head.points("name").asInstanceOf[List[String]])
  }

  def query(query: String): Future[QueryResponse] = {
    httpClient.get("/query", getQueryParameters(query))
      .map(response => QueryResponse.fromJson(response.content))
  }

  def queryWithoutResult(query: String): Future[EmptyResponse.type] = {
    httpClient.get("/query", getQueryParameters(query))
      .map(response => EmptyResponse)
  }

  protected def getQueryParameters(query: String) = Map("q" -> query)

  protected[influxdbclient] def getHttpClient = httpClient

}
