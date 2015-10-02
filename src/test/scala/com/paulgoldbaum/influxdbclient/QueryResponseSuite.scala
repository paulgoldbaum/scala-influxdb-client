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
    val data = JsonParser("""{"name":"test_series","columns":["column1", "column2", "column3"],"values":[["value1", 2, true]]}""")
    val series = QueryResponse.constructSeries(data)

    assert(series.name == "test_series")
    assert(series.columns == List("column1", "column2", "column3"))
    assert(series.records.length == 1)

    val record = series.records.head
    assert(record("column1") == "value1")
    assert(record("column2") == 2)
    assert(record("column3") == true)
  }

  test("Value series can be accessed by name") {
    val data = JsonParser("""{"name":"n","columns":["column1", "column2"],"values":[[1, 2],[2, 3],[3, 4],[4, 5]]}""")
    val series = QueryResponse.constructSeries(data)

    assert(series.points("column1") == List(1, 2, 3, 4))
    assert(series.points("column2") == List(2, 3, 4, 5))
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
