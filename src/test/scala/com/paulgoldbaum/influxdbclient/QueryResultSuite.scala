package com.paulgoldbaum.influxdbclient

import spray.json.JsonParser

class QueryResultSuite extends CustomTestSuite {

  test("Construct result") {
    val data = """{"results":[{"series":[{"name":"databases","columns":["name"],"values":[["_internal"]],"tags":{"tag": "value"}}]}]}"""
    val queryResponse = QueryResult.fromJson(data)

    assert(queryResponse.series.length == 1)
  }

  test("Construct record") {
    val data = JsonParser("""[1, "second value"]""")
    val record = QueryResult.constructRecord(Map("first_metric" -> 0, "second_metric" -> 1), data)
    assert(record(0) == 1)
    assert(record("first_metric") == 1)
    assert(record(1) == "second value")
    assert(record("second_metric") == "second value")
  }

  test("Constructing a record with unsupported types throws a MalformedResponseException") {
    try {
      val data = JsonParser( """[{}, "second value"]""")
      val record = QueryResult.constructRecord(Map("first_metric" -> 0, "second_metric" -> 1), data)
      fail("Exception not thrown")
    } catch {
      case e: MalformedResponseException =>
    }
  }

  test("Construct series") {
    val data = JsonParser("""{"name":"test_series","columns":["column1", "column2", "column3"],"values":[["value1", 2, true]],"tags":{"tag": "value"}}""")
    val series = QueryResult.constructSeries(data)

    assert(series.name == "test_series")
    assert(series.columns == List("column1", "column2", "column3"))
    assert(series.records.length == 1)
    assert(series.tags.size == 1)

    val record = series.records.head
    assert(record("column1") == "value1")
    assert(record("column2") == 2)
    assert(record("column3") == true)
  }

  test("Construct series without a name") {
    val data = JsonParser("""{"columns":["column1", "column2", "column3"],"values":[["value1", 2, true]],"tags":{"tag": "value"}}""")
    val series = QueryResult.constructSeries(data)

    assert(series.name == "")
    assert(series.columns == List("column1", "column2", "column3"))
    assert(series.records.length == 1)
    assert(series.tags.size == 1)
  }

  test("Construct series without values") {
    val data = JsonParser("""{"columns":["column1", "column2", "column3"]}""")
    val series = QueryResult.constructSeries(data)

    assert(series.name == "")
    assert(series.columns == List("column1", "column2", "column3"))
    assert(series.records.isEmpty)
  }

  test("Constructing a series with unsupported types throws a MalformedResponseException") {
    try {
      val data = JsonParser("""{"name":"test_series","columns":[1],"values":[]}""")
      val series = QueryResult.constructSeries(data)
      fail("Exception not thrown")
    } catch {
      case e: MalformedResponseException =>
    }
  }

  test("Value series can be accessed by name and position") {
    val data = JsonParser("""{"name":"n","columns":["column1", "column2"],"values":[[1, 2],[2, 3],[3, 4],[4, 5]]}""")
    val series = QueryResult.constructSeries(data)

    assert(series.points("column1") == List(1, 2, 3, 4))
    assert(series.points(0) == List(1, 2, 3, 4))
    assert(series.points("column2") == List(2, 3, 4, 5))
    assert(series.points(1) == List(2, 3, 4, 5))
  }

  test("Tags can be accessed by name") {
    val data = JsonParser("""{"columns":["column1", "column2", "column3"],"values":[["value1", 2, true]],"tags":{"tag": "value"}}""")
    val series = QueryResult.constructSeries(data)

    assert(series.tags("tag").toString == "value")
  }

  test("Valid error responses throws an ErrorResponseException") {
    val data = """{"results":[{"error":"database not found: _test"}]}"""
    try {
      QueryResult.fromJson(data)
      fail("Exception not thrown")
    } catch {
      case e: ErrorResponseException => // expected
    }
  }

  test("Empty responses return a QueryResponse with no series") {
    val data = """{"results":[{}]}"""
    val response = QueryResult.fromJson(data)
    assert(response.series.isEmpty)
  }
}
