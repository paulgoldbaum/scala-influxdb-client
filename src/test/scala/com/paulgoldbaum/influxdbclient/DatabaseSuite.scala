package com.paulgoldbaum.influxdbclient

import com.paulgoldbaum.influxdbclient.HttpClient.HttpResponse
import com.paulgoldbaum.influxdbclient.Parameters.{Consistency, Precision}
import org.scalatest.BeforeAndAfter

import scala.concurrent.Future

class DatabaseSuite extends CustomTestSuite with BeforeAndAfter {

  val influxdb = InfluxDB.connect()
  val database = influxdb.selectDatabase("_test")

  before {
    await(database.create())
  }

  after {
    await(database.drop())
  }

  test("Writing to a non-existent database throws a DatabaseNotFoundException") {
    val influxdb = InfluxDB.connect()
    val database = influxdb.selectDatabase("_test_database")

    try {
      await(database.write(Point("test_measurement").addField("value", 123)))
      fail("Exception not thrown")
    } catch {
      case e: DatabaseNotFoundException => // expected
    }
  }

  test("A point can be written and read") {
    await(database.write(Point("test_measurement").addField("value", 123)))
    val result = await(database.query("SELECT * FROM test_measurement"))
    assert(result.series.length == 1)
  }

  test("A point can be written with tags") {
    await(database.write(Point("test_measurement").addField("value", 123).addTag("tag_key", "tag_value")))
    val result = await(database.query("SELECT * FROM test_measurement WHERE tag_key='tag_value'"))
    assert(result.series.length == 1)
  }

  test("A point can be written and read with a precision parameter") {
    val time = 1444760421270l
    await(
      database.write(Point("test_measurement", time).addField("value", 123),
                     precision = Precision.MILLISECONDS))
    val result = await(database.query("SELECT * FROM test_measurement", Precision.MILLISECONDS))

    assert(result.series.length == 1)
  }

  test("A point can be written with a consistency parameter") {
    await(
      database.write(Point("test_measurement", 11111111).addField("value", 123),
        consistency = Consistency.ALL))
    val result = await(database.query("SELECT * FROM test_measurement"))
    assert(result.series.length == 1)
  }

  test("A point can be written with a retention policy parameter") {
    val retentionPolicyName = "custom_retention_policy"
    val measurementName = "test_measurement"
    await(database.createRetentionPolicy(retentionPolicyName, "1w", 1, default = false))
    await(
      database.write(Point(measurementName, 11111111).addField("value", 123),
        retentionPolicy = retentionPolicyName))
    val result = await(database.query("SELECT * FROM %s.%s".format(retentionPolicyName, measurementName)))
    assert(result.series.length == 1)
  }

  ignore("Writing to a non-existent retention policy throws an error") {
    // Makes influxdb explode. https://github.com/influxdb/influxdb/issues/4318
    try {
      await(
        database.write(Point("test_measurement", 11111111).addField("value", 123),
          retentionPolicy = "fake_retention_policy"))
      fail("Write using non-existent retention policy did not fail")
    } catch {
      case e: WriteException => // expected
    }
  }

  test("If an exception occurrs during a write, a WriteException is thrown") {
    val database = new Database("fake_name", new ExceptionThrowingHttpClient("", 1))
    try {
      await(database.write(new Point("point")))
      fail("Exception was not thrown")
    } catch {
      case e: WriteException => // expected
    }
  }

  test("If a 200 code is return during a write, a MalformedRequestException is thrown") {
    val database = new Database("fake_name", new ErrorReturningHttpClient("", 1, 200))
    try {
      await(database.write(new Point("point")))
      fail("Exception was not thrown")
    } catch {
      case e: RequestNotCompletedException => // expected
    }
  }

  test("If a 400 error occurrs during a write, a MalformedRequestException is thrown") {
    val database = new Database("fake_name", new ErrorReturningHttpClient("", 1, 400))
    try {
      await(database.write(new Point("point")))
      fail("Exception was not thrown")
    } catch {
      case e: MalformedRequestException => // expected
    }
  }

  test("If a 500 error occurrs during a write, a ServerUnavailableException is thrown") {
    val database = new Database("fake_name", new ErrorReturningHttpClient("", 1, 500))
    try {
      await(database.write(new Point("point")))
      fail("Exception was not thrown")
    } catch {
      case e: ServerUnavailableException => // expected
    }
  }

  class ExceptionThrowingHttpClient(host: String, port: Int) extends HttpClient(host, port) {
    override def post(url: String, params: Map[String, String] = Map(), content: String): Future[HttpResponse] = {
      Future.failed(new HttpException(""))
    }
  }

  class ErrorReturningHttpClient(host: String, port: Int, errorCode: Int) extends HttpClient(host, port) {
    override def post(url: String, params: Map[String, String] = Map(), content: String): Future[HttpResponse] = {
      Future.successful(new HttpResponse(errorCode, "Error message"))
    }
  }
}

