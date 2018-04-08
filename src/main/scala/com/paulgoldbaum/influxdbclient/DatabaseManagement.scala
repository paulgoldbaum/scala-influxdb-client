package com.paulgoldbaum.influxdbclient

protected[influxdbclient] trait DatabaseManagement { self: Database =>

  def create() =
    post("CREATE DATABASE \"" + databaseName + "\"")

  def drop() =
    post("DROP DATABASE \"" + databaseName + "\"")

  def exists() =
    showDatabases().map(_.contains(databaseName))

}
