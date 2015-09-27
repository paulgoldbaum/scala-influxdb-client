package com.paulgoldbaum.influxdbclient

class Client(val host: String, val port: Int, val username: String, val password: String) {

  def selectDatabase(databaseName: String) =
    new Database(host, port, username, password, databaseName)


}
