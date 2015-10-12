package com.paulgoldbaum.influxdbclient

class UserManagementSuite extends CustomTestSuite {

  val client = InfluxDB.connect()

  val username = "_test_username"
  val password = "test_password"

  test("A user can be created and dropped") {
    await(client.createUser(username, password))

    var users = await(client.showUsers())
    var usernames = users.series.head.points("user")
    assert(usernames.contains(username))

    await(client.dropUser(username))

    users = await(client.showUsers())
    usernames = users.series.head.points("user")
    assert(!usernames.contains(username))
  }

  test("Passwords are correctly escaped") {
    assert(client.escapePassword("pass'wor\nd") == "pass\\'wor\\\nd")
  }

  test("A user's password can be changed") {
    await(client.createUser(username, password))
    await(client.setUserPassword(username, "new_password"))
    await(client.dropUser(username))
  }

  test("Privileges can be granted to and revoked from a user") {
    await(client.createUser(username, password))
    await(client.grantPrivileges(username, "_test_database", ALL))
    await(client.revokePrivileges(username, "_test_database", WRITE))
    await(client.dropUser(username))
  }

  test("A user can be created as cluster admin") {
    await(client.createUser(username, password, true))
    await(client.showUsers())
    testIsClusterAdmin()
    await(client.dropUser(username))
  }

  test("A user can be made cluster admin") {
    await(client.createUser(username, password))
    await(client.makeClusterAdmin(username))
    testIsClusterAdmin()
    await(client.dropUser(username))
  }

  def testIsClusterAdmin() = {
    try {
      assert(await(client.userIsClusterAdmin(username)))
    } catch {
      case e: Throwable =>
        await(client.dropUser(username))
        throw e
    }
  }
}
