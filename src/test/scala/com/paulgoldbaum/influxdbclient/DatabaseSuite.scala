package com.paulgoldbaum.influxdbclient

import com.paulgoldbaum.influxdbclient.HttpClient.HttpResponse
import com.paulgoldbaum.influxdbclient.WriteParameters.{Precision, Consistency}
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.concurrent.{Future, Await}
import scala.concurrent.duration._

class DatabaseSuite extends FunSuite with BeforeAndAfter {

  val influxdb = InfluxDB.connect()
  val database = influxdb.selectDatabase("_test")
  val waitDuration = 1.second

  before {
    Await.result(database.create(), waitDuration)
  }

  after {
    Await.result(database.drop(), waitDuration)
  }

  test("A database can be created and dropped") {
    val influxdb = InfluxDB.connect()
    val database = influxdb.selectDatabase("_test_database")

    Await.result(database.create(), waitDuration)
    var databases = Await.result(database.showDatabases(), waitDuration)
    assert(databases.contains("_test_database"))

    Await.result(database.drop(), waitDuration)
    databases = Await.result(database.showDatabases(), waitDuration)
    assert(!databases.contains("_test_database"))
  }

  test("Writing to a non-existent database throws a DatabaseNotFoundException") {
    val influxdb = InfluxDB.connect()
    val database = influxdb.selectDatabase("_test_database")

    try {
      Await.result(database.write(Point("test_measurement").addField("value", 123)), waitDuration)
      fail("Exception not thrown")
    } catch {
      case e: DatabaseNotFoundException => // expected
    }
  }

  test("A point can be written and read") {
    Await.result(database.write(Point("test_measurement").addField("value", 123)), waitDuration)
    val result = Await.result(database.query("SELECT * FROM test_measurement"), waitDuration)
    assert(result.series.length == 1)
  }

  test("A point can be written with tags") {
    Await.result(database.write(Point("test_measurement").addField("value", 123).addTag("tag_key", "tag_value")), waitDuration)
    val result = Await.result(database.query("SELECT * FROM test_measurement WHERE tag_key='tag_value'"), waitDuration)
    assert(result.series.length == 1)
  }

  test("A point can be written with a precision parameter") {
    Await.result(
      database.write(Point("test_measurement", 11111111).addField("value", 123),
                     precision = Precision.NANOSECONDS),
      waitDuration)
    val result = Await.result(database.query("SELECT * FROM test_measurement"), waitDuration)
    assert(result.series.length == 1)
  }

  test("A point can be written with a consistency parameter") {
    Await.result(
      database.write(Point("test_measurement", 11111111).addField("value", 123),
        consistency = Consistency.ALL),
      waitDuration)
    val result = Await.result(database.query("SELECT * FROM test_measurement"), waitDuration)
    assert(result.series.length == 1)
  }

  test("A point can be written with a retention policy parameter") {
    Await.result(
      database.write(Point("test_measurement", 11111111).addField("value", 123),
        retentionPolicy = "default"),
      waitDuration)
    val result = Await.result(database.query("SELECT * FROM test_measurement"), waitDuration)
    assert(result.series.length == 1)
  }

  ignore("Writing to a non-existent retention policy throws an error") {
    // Makes influxdb explode. https://github.com/influxdb/influxdb/issues/4318
    try {
      Await.result(
        database.write(Point("test_measurement", 11111111).addField("value", 123),
          retentionPolicy = "fake_retention_policy"),
        waitDuration)
      fail("Write using non-existent retention policy did not fail")
    } catch {
      case e: WriteException => // expected
    }
  }

  test("If an exception occurrs during a write, a WriteException is thrown") {
    val database = new Database("fake_name", new ExceptionThrowingHttpClient("", 1))
    try {
      Await.result(database.write(new Point("point")), waitDuration)
      fail("Exception was not thrown")
    } catch {
      case e: WriteException => // expected
    }
  }

  test("If a 200 code is return during a write, a MalformedRequestException is thrown") {
    val database = new Database("fake_name", new ErrorReturningHttpClient("", 1, 200))
    try {
      Await.result(database.write(new Point("point")), waitDuration)
      fail("Exception was not thrown")
    } catch {
      case e: RequestNotCompletedException => // expected
    }
  }

  test("If a 400 error occurrs during a write, a MalformedRequestException is thrown") {
    val database = new Database("fake_name", new ErrorReturningHttpClient("", 1, 400))
    try {
      Await.result(database.write(new Point("point")), waitDuration)
      fail("Exception was not thrown")
    } catch {
      case e: MalformedRequestException => // expected
    }
  }

  test("If a 500 error occurrs during a write, a ServerUnavailableException is thrown") {
    val database = new Database("fake_name", new ErrorReturningHttpClient("", 1, 500))
    try {
      Await.result(database.write(new Point("point")), waitDuration)
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

