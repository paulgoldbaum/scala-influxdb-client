package com.paulgoldbaum.influxdbclient

object InfluxDB {

 def connect(host: String = "localhost", port: Int = 8086, username: String = null, password: String = null): Client = {
   new Client(host, port, username, password)
 }

}
