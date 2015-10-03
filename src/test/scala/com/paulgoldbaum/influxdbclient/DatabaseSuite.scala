package com.paulgoldbaum.influxdbclient

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

  test("Write parameters can be included") {
    /*
    database.write(Point("measurement").addField("value", 123),
      retentionPolicy = "retention_policy",
      precision = Precision.NANOSECONDS,
      consistency = Consistency.ALL
    )
    */
  }

  // Write to database that doesn't exist should throw error

  // Empty results show throw error
  // {"results":[{}]}

}
