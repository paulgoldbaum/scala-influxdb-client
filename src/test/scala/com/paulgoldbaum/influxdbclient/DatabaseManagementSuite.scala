package com.paulgoldbaum.influxdbclient

class DatabaseManagementSuite extends CustomTestSuite {

  test("A database can be created and dropped") {
    val database = influxdb.selectDatabase("_test_database")

    await(database.create())
    assert(await(database.exists()))

    await(database.drop())
    assert(!await(database.exists()))
  }

}
