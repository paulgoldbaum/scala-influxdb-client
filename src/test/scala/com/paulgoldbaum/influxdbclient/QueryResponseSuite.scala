package com.paulgoldbaum.influxdbclient

import org.scalatest.FunSuite

class QueryResponseSuite extends FunSuite {

  test("Can be created from a string") {
    val data = "{\"results\":[{\"series\":[{\"name\":\"databases\",\"columns\":[\"name\"],\"values\":[[\"_internal\"]]}]}]}"

    val queryResponse = QueryResponse.fromJson(data)
    //assert(queryResponse.series(0).values(0) == "_internal")
    assert(queryResponse.series(0))
  }



}
