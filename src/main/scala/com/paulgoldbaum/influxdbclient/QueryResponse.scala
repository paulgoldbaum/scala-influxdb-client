package com.paulgoldbaum.influxdbclient

import spray.json._
import DefaultJsonProtocol._

object QueryResponse {
  def fromJson(data: String) = {
    val root = data.parseJson
    //root.asJsObject.
  }
}

class QueryResponse(series: List[Series]) {

}

case class Series(name: String, columns: List[String], values: List[List[Any]])
case class RawResponse()

object QueryResponseJsonProtocol extends DefaultJsonProtocol {
  implicit val seriesFormat = jsonFormat3(Series)

}
