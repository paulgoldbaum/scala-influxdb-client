package com.paulgoldbaum.influxdbclient

import com.paulgoldbaum.influxdbclient.Mocks.ExceptionThrowingHttpClient

import scala.concurrent.Await

class InfluxDBSuite extends CustomTestSuite {

  test("Asking for a connection returns default parameters") {
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
    val database = new InfluxDB(new HttpClient("", 1, "", "")).selectDatabase("test_database")
    assert(database.databaseName == "test_database")
  }

  test("Shows existing databases") {
    val client = new InfluxDB(new HttpClient("localhost", 8086))
    val result = await(client.showDatabases())
    assert(result.contains("_internal"))
  }

  test("Server can be pinged") {
    val client = new InfluxDB(new HttpClient("localhost", 8086))
    await(client.ping())
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
}
