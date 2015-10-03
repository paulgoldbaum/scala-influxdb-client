package com.paulgoldbaum.influxdbclient

import com.paulgoldbaum.influxdbclient.WriteParameters.Consistency.Consistency
import com.paulgoldbaum.influxdbclient.WriteParameters.Precision.Precision

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

  def write(point: Point, precision: Precision = null, consistency: Consistency = null, retentionPolicy: String = null) = {
    var params = Map("db" -> databaseName)
    if (precision != null)
      params = params + ("precision" -> precision.str)
    if (consistency != null)
      params = params + ("consistency" -> consistency.str)
    if (retentionPolicy != null)
      params = params + ("retentionPolicy" -> retentionPolicy)

    httpClient.post("write", params, point.serialize())
  }

  override def query(query: String) = {
    val params = Map("q" -> query, "db" -> databaseName)
    httpClient.get("query", params)
      .map(response => QueryResponse.fromJson(response.content))
  }

}
