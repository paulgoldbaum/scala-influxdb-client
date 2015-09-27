package com.paulgoldbaum.influxdbclient

import org.scalatest.FunSuite

class InfluxDBSuite extends FunSuite {

  test("Asking for a connection returns default parameters") {
    val influxdb = InfluxDB.connect()

    assert(influxdb.host == "localhost")
    assert(influxdb.port == 8086)
    assert(influxdb.username == null)
    assert(influxdb.password == null)
  }

  test("Overridden parameters are returned in client") {
    val influxdb = InfluxDB.connect(
      host = "testdomain.com",
      port = 1234,
      username = "user",
      password = "password"
    )

    assert(influxdb.host == "testdomain.com")
    assert(influxdb.port == 1234)
    assert(influxdb.username == "user")
    assert(influxdb.password == "password")
  }

}
