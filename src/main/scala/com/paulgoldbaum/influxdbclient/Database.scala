package com.paulgoldbaum.influxdbclient

import com.paulgoldbaum.influxdbclient.WriteParameters.Consistency.Consistency
import com.paulgoldbaum.influxdbclient.WriteParameters.Precision.Precision

import scala.concurrent.Future

class Database protected[influxdbclient] (override val host: String,
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

  def write(point: Point,
            precision: Precision = null,
            consistency: Consistency = null,
            retentionPolicy: String = null): Future[Boolean] =
  {
    val params = buildWriteParameters(databaseName, precision, consistency, retentionPolicy)

    httpClient.post("write", params, point.serialize())
      .recover { case error => throw new WriteException("Error during write", error) }
      .map { result =>
        if (result.code != 204)
          throw new WriteException("Error during write: " + result.content, null)
        true
      }
  }

  private def buildWriteParameters(databaseName: String,
                                   precision: Precision = null,
                                   consistency: Consistency = null,
                                   retentionPolicy: String = null): Map[String, String] =
  {
    val params = List(("db", databaseName), ("precision", precision), ("consistency", consistency), ("rp", retentionPolicy))
    params.filterNot(_._2 == null).map(r => (r._1, r._2.toString)).toMap
  }

  override protected def getQueryParameters(query: String) = {
    super.getQueryParameters(query) + ("db" -> databaseName)
  }
}

class WriteException(str: String, throwable: Throwable) extends Exception(str, throwable)
