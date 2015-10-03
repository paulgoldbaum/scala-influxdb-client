package com.paulgoldbaum.influxdbclient

import com.paulgoldbaum.influxdbclient.WriteParameters.{Consistency, Precision}
import org.scalatest.{Matchers, FunSuite}

import scala.concurrent.Await
import scala.concurrent.duration._

class ClientSuite extends FunSuite with Matchers {

  test("Returns correct database") {
    val database = new Client("", 1, "", "").selectDatabase("test_database")
    assert(database.databaseName == "test_database")
  }

  test("Shows existing databases") {
    val client = new Client("localhost", 8086)
    val result = Await.result(client.showDatabases(), 2.seconds)
    assert(result.contains("_internal"))
  }

}
