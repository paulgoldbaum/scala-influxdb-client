package com.paulgoldbaum.influxdbclient

import org.scalatest.FunSuite
import spray.json.JsonParser

class QueryResponseSuite extends FunSuite {

  /*
  val data = "{\"results\":[{\"series\":[{\"name\":\"databases\",\"columns\":[\"name\"],\"values\":[[\"_internal\"]]}]}]}"
  val queryResponse = QueryResponse.fromJson(data)

  test("Name is parsed correctly") {
    assert(queryResponse.series.head.name == "databases")
  }

  test("Columns are parsed correctly") {
    assert(queryResponse.series.head.columns.length == 1)
    assert(queryResponse.series.head.columns.head == "name")
  }

  test("Values are accessible by position") {
    assert(queryResponse.series.head.values.head(0) == "_internal")
  }
  */

  test("Construct record") {
    val data = JsonParser("""[1, "second value"]""")
    val record = QueryResponse.constructRecord(Map("first_metric" -> 0, "second_metric" -> 1), data)
    assert(record(0) == 1)
    assert(record("first_metric") == 1)
    assert(record(1) == "second value")
    assert(record("second_metric") == "second value")
  }

  test("Construct series") {
    val data = JsonParser("""{"name":"databases","columns":["name"],"values":[["_internal"]]}""")
    val series = QueryResponse.constructSeries(data)

    assert(series.name == "databases")
    assert(series.columns.length == 1)
    assert(series.columns.head == "name")
    assert(series.records.length == 1)
    assert(series.records.head("name") == "_internal")
  }

  /*

  test("Invalid record value types throw exception") {}

  test("Values are accessible by name") {
    assert(queryResponse.series.head.values.head("name") == "_internal")
  }

  test("") {
    queryResponse.series.head.valuesByName("name") == Seq("_internal")
  }
  */
}
