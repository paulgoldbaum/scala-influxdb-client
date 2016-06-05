package com.paulgoldbaum.influxdbclient

import com.paulgoldbaum.influxdbclient.Parameter.Consistency.Consistency
import com.paulgoldbaum.influxdbclient.Parameter.Precision.Precision

import scala.concurrent.{ExecutionContext, Future}

class Database protected[influxdbclient]
(val databaseName: String, httpClient: HttpClient)
(override implicit protected val ec: ExecutionContext)
  extends InfluxDB(httpClient)
  with RetentionPolicyManagement
  with DatabaseManagement
{
  def write(point: Point,
            precision: Precision = null,
            consistency: Consistency = null,
            retentionPolicy: String = null): Future[Boolean] =
  {
    executeWrite(point.serialize(), precision, consistency, retentionPolicy)
  }

  def bulkWrite(points: Seq[Point],
            precision: Precision = null,
            consistency: Consistency = null,
            retentionPolicy: String = null): Future[Boolean] =
  {
    val payload = points.map(_.serialize()).mkString("\n")
    executeWrite(payload, precision, consistency, retentionPolicy)
  }

  private def executeWrite(payload: String, precision: Precision, consistency: Consistency, retentionPolicy: String) = {
    val params = buildWriteParameters(databaseName, precision, consistency, retentionPolicy)

    httpClient.post("/write", params, payload)
      .recover { case error: HttpException => throw exceptionFromStatusCode(error.code, "Error during write", error)}
      .map { result =>
        if (result.code != 204)
          throw exceptionFromStatusCode(result.code, "Error during write: " + result.content)
        true
      }
  }

  private def buildWriteParameters(databaseName: String,
                                   precision: Precision = null,
                                   consistency: Consistency = null,
                                   retentionPolicy: String = null): Map[String, String] =
  {
    val params = List(("db", databaseName),
      ("precision", precision),
      ("consistency", consistency),
      ("rp", retentionPolicy))
    params.filterNot(_._2 == null).map(r => (r._1, r._2.toString)).toMap
  }

  override protected def buildQueryParameters(query: String, precision: Precision) =
    super.buildQueryParameters(query, precision) + ("db" -> databaseName)

  protected def exceptionFromStatusCode(statusCode: Int, str: String, throwable: Throwable = null): WriteException =
    statusCode match {
      case 200 => new RequestNotCompletedException(str, throwable)
      case 404 => new DatabaseNotFoundException(str, throwable)
      case e if 400 <= e && e <= 499 => new MalformedRequestException(str, throwable)
      case e if 500 <= e && e <= 599 => new ServerUnavailableException(str, throwable)
      case _ => new UnknownErrorException(str, throwable)
  }
}


abstract class WriteException(str: String, throwable: Throwable = null) extends InfluxDBException(str, throwable)
class DatabaseNotFoundException(str: String, throwable: Throwable) extends WriteException(str, throwable)
class MalformedRequestException(str: String, throwable: Throwable) extends WriteException(str, throwable)
class RequestNotCompletedException(str: String, throwable: Throwable) extends WriteException(str, throwable)
class ServerUnavailableException(str: String, throwable: Throwable) extends WriteException(str, throwable)
class UnknownErrorException(str: String, throwable: Throwable) extends WriteException(str, throwable)
