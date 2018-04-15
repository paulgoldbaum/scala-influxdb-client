package com.paulgoldbaum.influxdbclient

protected[influxdbclient] trait DatabaseManagement { self: Database =>

  def create() =
    exec("CREATE DATABASE \"" + databaseName + "\"")

  def drop() =
    exec("DROP DATABASE \"" + databaseName + "\"")

  def exists() =
    showDatabases().map(_.contains(databaseName))

}
