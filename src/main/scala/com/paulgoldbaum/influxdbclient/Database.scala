package com.paulgoldbaum.influxdbclient

class Database(override val host: String,
               override val port: Int,
               override val username: String,
               override val password: String,
               val databaseName: String) extends Client(host, port, username, password)
{

  def create() = {
    query("CREATE DATABASE \"" + databaseName + "\"")
  }

  def drop() = {
    query("DROP DATABASE \"" + databaseName + "\"")
  }

}
