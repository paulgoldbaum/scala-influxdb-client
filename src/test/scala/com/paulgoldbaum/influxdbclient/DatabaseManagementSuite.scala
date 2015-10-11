package com.paulgoldbaum.influxdbclient

import org.scalatest.FunSuite

import scala.concurrent.Await
import scala.concurrent.duration._

class DatabaseManagementSuite extends FunSuite {

  val waitDuration = 1.second

  test("A database can be created and dropped") {
    val influxdb = InfluxDB.connect()
    val database = influxdb.selectDatabase("_test_database")

    Await.result(database.create(), waitDuration)
    var databases = Await.result(database.showDatabases(), waitDuration)
    assert(databases.contains("_test_database"))

    Await.result(database.drop(), waitDuration)
    databases = Await.result(database.showDatabases(), waitDuration)
    assert(!databases.contains("_test_database"))
  }

}
