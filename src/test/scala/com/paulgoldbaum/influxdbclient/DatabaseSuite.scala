package com.paulgoldbaum.influxdbclient

import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.concurrent.Await
import scala.concurrent.duration._

class DatabaseSuite extends FunSuite with BeforeAndAfter {

  val influxdb = InfluxDB.connect()
  val database = influxdb.selectDatabase("_test")

  test("A database can be created and dropped") {
    val influxdb = InfluxDB.connect()
    val database = influxdb.selectDatabase("test_database")

    Await.result(database.create(), 1.second)
    var databases = Await.result(influxdb.showDatabases(), 1.second)
    assert(databases.contains("test_database"))

    Await.result(database.drop(), 1.second)
    databases = Await.result(influxdb.showDatabases(), 1.second)
    assert(!databases.contains("test_database"))
  }

  before {
    database.create()
  }

  after {
    database.drop()
  }

  test("A point can be written and read") {
    database.write(Point("test_measurement").addField("value", 123))
    val result = Await.result(database.query("SELECT * FROM test_measurement"), 1.second)
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
