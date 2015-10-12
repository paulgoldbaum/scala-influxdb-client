package com.paulgoldbaum.influxdbclient

protected[influxdbclient] trait DatabaseManagement { self: Database =>

  def create() =
    query("CREATE DATABASE \"" + databaseName + "\"")

  def drop() =
    query("DROP DATABASE \"" + databaseName + "\"")

}
