package com.paulgoldbaum.influxdbclient

import scala.concurrent.ExecutionContext

class Client(val host: String, val port: Int, val username: String = null, val password: String = null) {

  val httpClient = new HttpClient(host, port, username, password)
  implicit val ec = ExecutionContext.global

  def selectDatabase(databaseName: String) =
    new Database(host, port, username, password, databaseName)

  def showDatabases() = {
    query("SHOW DATABASES").map(_.content)
  }

  def query(query: String) = {
    val params = Map("q" -> query)
    httpClient.get("query", params)
  }

}
