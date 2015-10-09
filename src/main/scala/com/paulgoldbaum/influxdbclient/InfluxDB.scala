package com.paulgoldbaum.influxdbclient

object InfluxDB {

 def connect(host: String = "localhost",
             port: Int = 8086,
             username: String = null,
             password: String = null): Client =
 {
   val httpClient = new HttpClient(host, port, username, password)
   new Client(httpClient)
 }

}
