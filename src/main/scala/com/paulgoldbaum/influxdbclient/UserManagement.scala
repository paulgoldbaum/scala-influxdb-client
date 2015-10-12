package com.paulgoldbaum.influxdbclient

protected[influxdbclient] trait UserManagement { self: Client =>
  def createUser(username: String, password: String, isClusterAdmin: Boolean = false) = {
    var query = "CREATE USER %s WITH PASSWORD '%s'".format(username, password)
    if (isClusterAdmin)
      query = query + " WITH ALL PRIVILEGES"
    queryWithoutResult(query)
  }

  def dropUser(username: String) = {
    val query = "DROP USER " + username
    queryWithoutResult(query)
  }

  def showUsers() =
    query("SHOW USERS")

  def setUserPassword(username: String, password: String) =
    query("SET PASSWORD FOR %s='%s'".format(username, password))

  def grantPrivileges(username: String, database: String, privilege: Privilege) =
    query("GRANT %s ON %s TO %s".format(privilege, database, username))

  def revokePrivileges(username: String, database: String, privilege: Privilege) =
    query("REVOKE %s ON %s FROM %s".format(privilege, database, username))

  def makeClusterAdmin(username: String) =
    query("GRANT ALL PRIVILEGES TO %s".format(username))

  def userIsClusterAdmin(username: String) = {
    showUsers().map(result =>
      result.series.head.records.exists(record =>
        record("user") == username && record("admin") == true))
  }

  protected[influxdbclient] def escapePassword(password: String) =
    password.replaceAll("(['\n])", "\\\\$1")
}

sealed trait Privilege
case object READ extends Privilege
case object WRITE extends Privilege
case object ALL extends Privilege
