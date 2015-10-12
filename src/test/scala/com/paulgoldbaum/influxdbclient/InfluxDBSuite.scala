package com.paulgoldbaum.influxdbclient

class InfluxDBSuite extends CustomTestSuite {

  test("Asking for a connection returns default parameters") {
    val influxdb = InfluxDB.connect()
    val httpClient = influxdb.getHttpClient

    assert(httpClient.host == "localhost")
    assert(httpClient.port == 8086)
    assert(httpClient.username == null)
    assert(httpClient.password == null)
  }

  test("Overridden parameters are returned in client") {
    val influxdb = InfluxDB.connect(
      host = "testdomain.com",
      port = 1234,
      username = "user",
      password = "password"
    )
    val httpClient = influxdb.getHttpClient

    assert(httpClient.host == "testdomain.com")
    assert(httpClient.port == 1234)
    assert(httpClient.username == "user")
    assert(httpClient.password == "password")
  }

}
