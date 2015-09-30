package com.paulgoldbaum.influxdbclient

import org.scalatest.FunSuite

class DatabaseSuite extends FunSuite {

  /*
  test("A database can be created") {
    val influxdb = InfluxDB.connect()
    val database = influxdb.selectDatabase("test_database")

    database.create()



  }
  */

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
