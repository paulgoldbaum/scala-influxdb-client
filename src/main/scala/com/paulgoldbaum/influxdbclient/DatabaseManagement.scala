package com.paulgoldbaum.influxdbclient

protected[influxdbclient] trait DatabaseManagement { self: Database =>

  def create(ifNotExists: Boolean = false) = {
    val queryString = new StringBuilder()
    queryString ++= "CREATE DATABASE "
    if (ifNotExists)
      queryString ++= "IF NOT EXISTS "
    queryString ++= "\"" + databaseName + "\""
    query(queryString.mkString)
  }

  def drop() =
    query("DROP DATABASE \"" + databaseName + "\"")

  def exists() =
    showDatabases().map(_.contains(databaseName))

}
