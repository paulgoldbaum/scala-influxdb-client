package com.paulgoldbaum.influxdbclient

import com.paulgoldbaum.influxdbclient.WriteParameters.{Consistency, Precision}
import org.scalatest.FunSuite

class ClientSuite extends FunSuite {

  test("Returns correct database") {
    val database = new Client("", 1, "", "").selectDatabase("test_database")
    assert(database.databaseName == "test_database")
  }

}
