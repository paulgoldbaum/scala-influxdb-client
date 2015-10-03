package com.paulgoldbaum.influxdbclient

class Database(override val host: String,
               override val port: Int,
               override val username: String,
               override val password: String,
               val databaseName: String) extends Client(host, port, username, password)
{

  def create() = {
    queryWithoutResult("CREATE DATABASE \"" + databaseName + "\"")
  }

  def drop() = {
    queryWithoutResult("DROP DATABASE \"" + databaseName + "\"")
  }

  def write(point: Point) = {
    val params = Map("db" -> databaseName)
    httpClient.post("write", params, point.serialize())
  }

  override def query(query: String) = {
    val params = Map("q" -> query, "db" -> databaseName)
    httpClient.get("query", params)
      .map(response => QueryResponse.fromJson(response.content))
  }

}
