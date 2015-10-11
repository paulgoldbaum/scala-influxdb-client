package com.paulgoldbaum.influxdbclient

import org.scalatest.{Matchers, FunSuite}

import scala.concurrent.Await
import scala.concurrent.duration._

class ClientSuite extends FunSuite with Matchers {

  test("Returns correct database") {
    val database = new Client(new HttpClient("", 1, "", "")).selectDatabase("test_database")
    assert(database.databaseName == "test_database")
  }

  test("Shows existing databases") {
    val client = new Client(new HttpClient("localhost", 8086))
    val result = Await.result(client.showDatabases(), 2.seconds)
    assert(result.contains("_internal"))
  }

  ignore("A retention policy can be used while reading") {
  }

}
