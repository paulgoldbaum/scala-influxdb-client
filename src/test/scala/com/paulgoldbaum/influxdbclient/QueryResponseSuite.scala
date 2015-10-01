package com.paulgoldbaum.influxdbclient

import org.scalatest.FunSuite

class QueryResponseSuite extends FunSuite {

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

  /*
  test("Values are accessible by name") {
    assert(queryResponse.series.head.values.head("name") == "_internal")
  }
  */
}
