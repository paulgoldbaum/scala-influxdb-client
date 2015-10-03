package com.paulgoldbaum.influxdbclient

import com.paulgoldbaum.influxdbclient.WriteParameters.{Precision, Consistency}
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.concurrent.Await
import scala.concurrent.duration._

class DatabaseSuite extends FunSuite with BeforeAndAfter {

  val influxdb = InfluxDB.connect()
  val database = influxdb.selectDatabase("_test")
  val waitDuration = 1.second

  test("A database can be created and dropped") {
    val influxdb = InfluxDB.connect()
    val database = influxdb.selectDatabase("test_database")

    Await.result(database.create(), waitDuration)
    var databases = Await.result(database.showDatabases(), waitDuration)
    assert(databases.contains("test_database"))

    Await.result(database.drop(), waitDuration)
    databases = Await.result(database.showDatabases(), waitDuration)
    assert(!databases.contains("test_database"))
  }

  before {
    Await.result(database.create(), waitDuration)
  }

  after {
    Await.result(database.drop(), waitDuration)
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
    fail("this test should blow up when using a non-existent retention policy")
    Await.result(
      database.write(Point("test_measurement", 11111111).addField("value", 123),
        retentionPolicy = "custom_retention_policy"),
      waitDuration)
    val result = Await.result(database.query("SELECT * FROM test_measurement"), waitDuration)
    assert(result.series.length == 1)
  }

  // Write to database that doesn't exist should throw error

  // Empty results show throw error
  // {"results":[{}]}

}
