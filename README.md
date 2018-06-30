scala-influxdb-client
=====================

[![Build Status](https://travis-ci.org/paulgoldbaum/scala-influxdb-client.svg?branch=master)](https://travis-ci.org/paulgoldbaum/scala-influxdb-client)
[![codecov.io](http://codecov.io/github/paulgoldbaum/scala-influxdb-client/coverage.svg?branch=master)](http://codecov.io/github/paulgoldbaum/scala-influxdb-client?branch=master)

Asynchronous library for accessing InfluxDB from Scala.

## Installation
Add the following to your `build.sbt`
```scala
libraryDependencies += "com.paulgoldbaum" %% "scala-influxdb-client" % "0.6.1"
```
**NOTE**: Starting with version 0.5.0 JDK 8 is required.

## Connecting
```scala
import com.paulgoldbaum.influxdbclient._
import scala.concurrent.ExecutionContext.Implicits.global

val influxdb = InfluxDB.connect("localhost", 8086)
```

And when all done close the client:
 
```scala
influxdb.close()
```

## Usage
All methods are non-blocking and return a `Future`; in most cases a `Future[QueryResponse]` which might be empty if
the action does not return a result. Failing `Futures` carry a subclass of `InfluxDBException`

### Working with databases
```scala
val database = influxdb.selectDatabase("my_database")
database.exists() // => Future[Boolean]
database.create()
database.drop()
```

### Writing data 
```scala
val point = Point("cpu")
  .addTag("host", "my.server")
  .addField("1m", 0.3)
  .addField("5m", 0.4)
  .addField("15m", 0.5)
database.write(point)
```
Additionally, timestamp precision, consistency and custom retention policies can be specified
```scala
val point = Point("cpu", System.currentTimeMillis())
database.write(point,
               precision = Precision.MILLISECONDS,
               consistency = Consistency.ALL, 
               retentionPolicy = "custom_rp")
```
If no precision parameter is given, InfluxDB assumes timestamps to be in nanoseconds.

If a write fails, it's future will contain a subclass of `WriteException`. This can be handled through the usual
methods of error handling in `Futures`, i.e.
```scala
database.write(point)
  // ...
  .recover{ case e: WriteException => ...}
```

Multiple points can be written in one operation by using the bulkWrite operation
```scala
val points = List(
  Point("test_measurement1").addField("value1", 123),
  Point("test_measurement2").addField("value2", 123),
  Point("test_measurement3").addField("value3", 123)
)
database.bulkWrite(points, precision = Precision.MILLISECONDS)
```

### Querying the database
Given the following data:
```
name: cpu
---------
time                            host     region   value
2015-10-14T18:31:14.744203449Z	serverA  us_west  0.64
2015-10-14T18:31:19.242472211Z	serverA  us_west  0.85
2015-10-14T18:31:22.644254309Z	serverA  us_west  0.43
```
```scala
database.query("SELECT * FROM cpu")
```
This returns a `Future[QueryResult]`. To access the list of records use
```scala
result.series.head.records
```
which we can iterate to access the different fields
```scala
result.series.head.records.foreach(record => record("host"))
```
For each record, we can access all it's values at once using the allValues property
```scala
result.series.head.records(0).allValues
```

If we are only interested in the "value" field of each record
```scala
result.series.head.points("value")
```
returns a list of just the `value` field of each record.

The list of column names can be accessed through
```scala
result.series.head.columns
```

Multiple queries can be sent to the server at the same time using the `multiQuery` method
```scala
database.multiQuery(List("SELECT * FROM cpu LIMIT 5", "SELECT * FROM cpu LIMIT 5 OFFSET 5"))
```

In this case, the result is a `Future[List[QueryResult]]`.

Errors during queries return a `QueryException`.

### Executing actions
To execute any action that is not covered by the API you can use the `exec` method
```scala
influxdb.exec("CREATE CONTINUOUS QUERY ...")
```

### Managing users
```scala
influxdb.createUser(username, password, isClusterAdmin)
influxdb.dropUser(username)
influxdb.showUsers()
influxdb.setUserPassword(username, password)
influxdb.grantPrivileges(username, database, privilege)
influxdb.revokePrivileges(username, database, privilege)
influxdb.makeClusterAdmin(username)
influxdb.userIsClusterAdmin(username)
```

### Managing retention policies
```scala
database.createRetentionPolicy(name, duration, replication, default)
database.showRetentionPolicies()
database.dropRetentionPolicy(name)
database.alterRetentionPolicy(name, duration, replication, default)
```

**NOTE**: User and retention policy management primitives return an empty `QueryResult` or fail with a `QueryException` in case of an error.

### Writing over UDP
```scala
import com.paulgoldbaum.influxdbclient._

val udpClient = InfluxDB.udpConnect("localhost", 8086)
val point = Point("cpu", System.currentTimeMillis())
udpClient.write(point)
```
Points can also be written in bulk
```scala
val points = List(
  Point("test_measurement1").addField("value1", 123),
  Point("test_measurement2").addField("value2", 123),
  Point("test_measurement3").addField("value3", 123)
)
udpClient.bulkWrite(points)
```
