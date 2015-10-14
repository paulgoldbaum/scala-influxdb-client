scala-influxdb-client
=====================

[![Build Status](https://travis-ci.org/paulgoldbaum/scala-influxdb-client.svg?branch=master)](https://travis-ci.org/paulgoldbaum/scala-influxdb-client)
[![codecov.io](http://codecov.io/github/paulgoldbaum/scala-influxdb-client/coverage.svg?branch=master)](http://codecov.io/github/paulgoldbaum/scala-influxdb-client?branch=master)

Asynchronous library for accessing InfluxDB from Scala.

## Connecting
```scala
import com.paulgoldbaum.influxdbclient._

val influxdb = InfluxDB.connect("localhost", 8086)
```

## Usage
All methods are non-blocking and return a `Future`, in most cases a `Future[QueryResponse]` which might be empty if the action does not return a result.

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

If a write fails, a subclass of `WriteException` will be thrown. This can be handled through the usual methods of 
error handling in `Futures`, i.e.:
```scala
database.write(point)
  .map(...)
  .recover{ case e: WriteException => ...}
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
This returns a Future[QueryResult]. To access the list of records use
```scala
result.series.head.records
```
which we can iterate and access the different fields:
```scala
result.series.head.records.foreach(record => record("host"))
```

If we are only interested in the "value" field of each record
```scala
result.series.head.points("value")
```
returns a list of just the value field of each record.

The column names can be accessed through
```scala
result.series.head.columns
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


