package com.paulgoldbaum.influxdbclient

class DatabaseManagementSuite extends CustomTestSuite {

  test("A database can be created and dropped") {
    val database = influxdb.selectDatabase("_test_database_mgmnt")

    await(database.create())
    assert(await(database.exists()))

    await(database.drop())
    assert(!await(database.exists()))
  }

  test("Creating a database with ifNotExists does not fail if the database already exists") {
    val database = influxdb.selectDatabase("_test_database_mgmnt")

    await(database.create())
    val result = await(database.create(ifNotExists = true))
    assert(result.series.isEmpty)

    await(database.drop())
  }

}
