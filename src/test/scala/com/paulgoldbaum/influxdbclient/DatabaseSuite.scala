package com.paulgoldbaum.influxdbclient

import org.scalatest.FunSuite

import scala.concurrent.Await
import scala.concurrent.duration._

class DatabaseSuite extends FunSuite {

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

  test("A point can be written and read") {
    /*

    val influxdb = InfluxDB.connect()

    val database = influxdb.selectDatabase("test_database")

    database.write(Point("measurement").addField("value", 123))

    database.query("aeuaoeuaoeu aoeuaoeu")
    */
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

}
