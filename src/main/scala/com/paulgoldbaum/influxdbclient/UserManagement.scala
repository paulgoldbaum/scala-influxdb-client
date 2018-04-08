package com.paulgoldbaum.influxdbclient

protected[influxdbclient] trait UserManagement { self: InfluxDB =>
  def createUser(username: String, password: String, isClusterAdmin: Boolean = false) = {
    var queryString = "CREATE USER %s WITH PASSWORD '%s'".format(username, password)
    if (isClusterAdmin)
      queryString = queryString + " WITH ALL PRIVILEGES"
    post(queryString)
  }

  def dropUser(username: String) = {
    val queryString = "DROP USER " + username
    post(queryString)
  }

  def showUsers() =
    query("SHOW USERS")

  def setUserPassword(username: String, password: String) =
    post("SET PASSWORD FOR %s='%s'".format(username, password))

  def grantPrivileges(username: String, database: String, privilege: Privilege) =
    post("GRANT %s ON %s TO %s".format(privilege, database, username))

  def revokePrivileges(username: String, database: String, privilege: Privilege) =
    post("REVOKE %s ON %s FROM %s".format(privilege, database, username))

  def makeClusterAdmin(username: String) =
    post("GRANT ALL PRIVILEGES TO %s".format(username))

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
