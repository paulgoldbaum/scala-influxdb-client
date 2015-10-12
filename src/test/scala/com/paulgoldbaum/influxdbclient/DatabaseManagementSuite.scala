package com.paulgoldbaum.influxdbclient

class DatabaseManagementSuite extends CustomTestSuite {

  test("A database can be created and dropped") {
    val influxdb = InfluxDB.connect()
    val database = influxdb.selectDatabase("_test_database")

    await(database.create())
    var databases = await(database.showDatabases())
    assert(databases.contains("_test_database"))

    await(database.drop())
    databases = await(database.showDatabases())
    assert(!databases.contains("_test_database"))
  }

}
