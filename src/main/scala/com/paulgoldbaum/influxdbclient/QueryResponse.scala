package com.paulgoldbaum.influxdbclient

import spray.json._

case class Series(name: String, columns: List[String], values: List[List[String]])

object QueryResponseJsonProtocol extends DefaultJsonProtocol {
  implicit val seriesFormat = jsonFormat3(Series)
}

object QueryResponse {
  import QueryResponseJsonProtocol._

  def fromJson(data: String) = {
    val root = data.parseJson.asInstanceOf[JsObject]
    val resultsArray = root.fields("results").asInstanceOf[JsArray]
    val resultObject = resultsArray.elements.head.asInstanceOf[JsObject]
    val seriesArray = resultObject.fields("series").asInstanceOf[JsArray]

    val series = seriesArray.elements.map(_.convertTo[Series]).toList
    new QueryResponse(series)
  }
}

class QueryResponse(val series: List[Series]) {

}
