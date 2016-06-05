package com.paulgoldbaum.influxdbclient

class UserManagementSuite extends CustomTestSuite {

  val username = "_test_username"
  val password = "test_password"

  test("A user can be created and dropped") {
    await(influxdb.createUser(username, password))

    var users = await(influxdb.showUsers())
    var usernames = users.series.head.points("user")
    assert(usernames.contains(username))

    await(influxdb.dropUser(username))

    users = await(influxdb.showUsers())
    usernames = users.series.head.points("user")
    assert(!usernames.contains(username))
  }

  test("Passwords are correctly escaped") {
    assert(influxdb.escapePassword("pass'wor\nd") == "pass\\'wor\\\nd")
  }

  test("A user's password can be changed") {
    await(influxdb.createUser(username, password))
    await(influxdb.setUserPassword(username, "new_password"))
    await(influxdb.dropUser(username))
  }

  test("Privileges can be granted to and revoked from a user") {
    await(influxdb.createUser(username, password))
    await(influxdb.grantPrivileges(username, "_test_database", ALL))
    await(influxdb.revokePrivileges(username, "_test_database", WRITE))
    await(influxdb.dropUser(username))
  }

  test("A user can be created as cluster admin") {
    await(influxdb.createUser(username, password, true))
    await(influxdb.showUsers())
    testIsClusterAdmin()
    await(influxdb.dropUser(username))
  }

  test("A user can be made cluster admin") {
    await(influxdb.createUser(username, password))
    await(influxdb.makeClusterAdmin(username))
    testIsClusterAdmin()
    await(influxdb.dropUser(username))
  }

  def testIsClusterAdmin() = {
    try {
      assert(await(influxdb.userIsClusterAdmin(username)))
    } catch {
      case e: Throwable =>
        await(influxdb.dropUser(username))
        throw e
    }
  }
}
