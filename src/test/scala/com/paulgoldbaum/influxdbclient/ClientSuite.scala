package com.paulgoldbaum.influxdbclient

class ClientSuite extends CustomTestSuite {

  test("Returns correct database") {
    val database = new Client(new HttpClient("", 1, "", "")).selectDatabase("test_database")
    assert(database.databaseName == "test_database")
  }

  test("Shows existing databases") {
    val client = new Client(new HttpClient("localhost", 8086))
    val result = await(client.showDatabases())
    assert(result.contains("_internal"))
  }

  ignore("A retention policy can be used while reading") {
  }

}
