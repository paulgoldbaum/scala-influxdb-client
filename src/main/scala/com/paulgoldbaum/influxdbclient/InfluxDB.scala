package com.paulgoldbaum.influxdbclient

import scala.concurrent.{Future, ExecutionContext}

object InfluxDB {

 def connect(host: String = "localhost",
             port: Int = 8086,
             username: String = null,
             password: String = null): InfluxDB =
 {
   val httpClient = new HttpClient(host, port, username, password)
   new InfluxDB(httpClient)
 }
}

class InfluxDB protected[influxdbclient](httpClient: HttpClient) extends Object with UserManagement {

  implicit val ec = ExecutionContext.global

  def selectDatabase(databaseName: String) =
    new Database(databaseName, httpClient)

  def showDatabases(): Future[Seq[String]] = {
    query("SHOW DATABASES")
      .map(response => response.series.head.points("name").asInstanceOf[List[String]])
  }

  def query(query: String): Future[QueryResult] = {
    httpClient.get("/query", buildQueryParameters(query))
      .map(response => QueryResult.fromJson(response.content))
  }

  protected def buildQueryParameters(query: String) = Map("q" -> query)

  protected[influxdbclient] def getHttpClient = httpClient

}
