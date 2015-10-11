package com.paulgoldbaum.influxdbclient

trait DatabaseManagement { self: Database =>

  def create() = {
    queryWithoutResult("CREATE DATABASE \"" + databaseName + "\"")
  }

  def drop() = {
    queryWithoutResult("DROP DATABASE \"" + databaseName + "\"")
  }


}
