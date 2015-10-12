package com.paulgoldbaum.influxdbclient

import org.scalatest.FunSuite

import scala.concurrent.Await
import scala.concurrent.duration._

class UserManagementSuite extends FunSuite {

  val client = InfluxDB.connect()

  val username = "_test_username"
  val password = "test_password"
  val timeout = 1.second

  test("A user can be created and dropped") {
    Await.result(client.createUser(username, password), timeout)

    var users = Await.result(client.showUsers(), timeout)
    var usernames = users.series.head.points("user")
    assert(usernames.contains(username))

    Await.result(client.dropUser(username), timeout)

    users = Await.result(client.showUsers(), timeout)
    usernames = users.series.head.points("user")
    assert(!usernames.contains(username))
  }

  test("Passwords are correctly escaped") {
    assert(client.escapePassword("pass'wor\nd") == "pass\\'wor\\\nd")
  }

  test("A user's password can be changed") {
    Await.result(client.createUser(username, password), timeout)
    Await.result(client.setUserPassword(username, "new_password"), timeout)
    Await.result(client.dropUser(username), timeout)
  }

  test("Privileges can be granted to and revoked from a user") {
    Await.result(client.createUser(username, password), timeout)
    Await.result(client.grantPrivileges(username, "_test_database", ALL), timeout)
    Await.result(client.revokePrivileges(username, "_test_database", WRITE), timeout)
    Await.result(client.dropUser(username), timeout)
  }

  test("A user can be created as cluster admin") {
    Await.result(client.createUser(username, password, true), timeout)
    Await.result(client.showUsers(), timeout)
    testIsClusterAdmin()
    Await.result(client.dropUser(username), timeout)
  }

  test("A user can be made cluster admin") {
    Await.result(client.createUser(username, password), timeout)
    Await.result(client.makeClusterAdmin(username), timeout)
    testIsClusterAdmin()
    Await.result(client.dropUser(username), timeout)
  }

  def testIsClusterAdmin() = {
    try {
      assert(Await.result(client.userIsClusterAdmin(username), timeout))
    } catch {
      case e: Throwable =>
        Await.result(client.dropUser(username), timeout)
        throw e
    }
  }
}
