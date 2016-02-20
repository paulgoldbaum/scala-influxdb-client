package com.paulgoldbaum.influxdbclient

import com.paulgoldbaum.influxdbclient.Mocks.ExceptionThrowingHttpClient

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

  test("Returns correct database") {
    val database = new InfluxDB(new HttpClient("", 1, false, "", "")).selectDatabase("test_database")
    assert(database.databaseName == "test_database")
  }

  test("Shows existing databases") {
    val result = await(influxdb.showDatabases())
    assert(result.contains("_internal"))
  }

  test("Server can be pinged") {
    await(influxdb.ping())
  }

  test("If an error happens during a ping a PingException is thrown") {
    val client = new InfluxDB(new ExceptionThrowingHttpClient("", 0))
    try {
      await(client.ping())
      fail("Exception not thrown")
    } catch {
      case e: PingException => // expected
    }
  }

  test("If an error happens during a query a QueryException is thrown") {
    val client = new InfluxDB(new ExceptionThrowingHttpClient("", 0))
    try {
      await(client.query(""))
      fail("Exception not thrown")
    } catch {
      case e: QueryException => // expected
    }
  }

  test("Multiple queries can be executed at the same time") {
    val queries = List("select * from subscriber limit 5", "select * from \"write\" limit 5")
    val results = await(influxdb.selectDatabase("_internal").multiQuery(queries))
    assert(results.length == 2)
    assert(results(0).series.head.name == "subscriber")
    assert(results(1).series.head.name == "write")
  }

  test("Connections can be closed") {
    val influxdb = InfluxDB.connect()
    influxdb.close()
    val httpClient = influxdb.getHttpClient

    assert(httpClient.isClosed)
  }
}
