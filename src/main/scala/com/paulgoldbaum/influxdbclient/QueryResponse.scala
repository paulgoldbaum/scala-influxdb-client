package com.paulgoldbaum.influxdbclient

import spray.json._

class Record protected[influxdbclient]
(namesIndex: Map[String, Int], values: List[Any]) {
  def apply(position: Int) = values(position)
  def apply(name: String) = values(namesIndex(name))
}

class Series protected[influxdbclient]
(val name: String, val columns: List[String], val records: List[Record]) {
  def points(column: String) = records.map(_(column))
  def points(column: Int) = records.map(_(column))
}

object QueryResponse {

  protected[influxdbclient]
  def fromJson(data: String) = {
    val root = data.parseJson.asInstanceOf[JsObject]
    val resultsArray = root.fields("results").asInstanceOf[JsArray]
    val resultObject = resultsArray.elements.head.asInstanceOf[JsObject]
    val fields = resultObject.fields
    if (fields.contains("error")) {
      throw new ErrorResponseException(fields("error").toString())
    }
    val seriesArray = fields("series").asInstanceOf[JsArray]

    val series = seriesArray.elements.map(constructSeries).toList
    new QueryResponse(series)
  }

  protected[influxdbclient]
  def constructSeries(value: JsValue): Series = {
    val fields = value.asInstanceOf[JsObject].fields
    val seriesName = fields("name").asInstanceOf[JsString].value
    val columns = fields("columns").asInstanceOf[JsArray].elements.map {
      case JsString(column) => column
      case t => throw new MalformedResponseException("Found invalid type " + t.toString())
    }.toList

    val namesIndex = columns.zipWithIndex.toMap
    val records = fields("values").asInstanceOf[JsArray].elements.map(constructRecord(namesIndex, _)).toList
    new Series(seriesName, columns, records)
  }

  protected[influxdbclient]
  def constructRecord(namesIndex: Map[String, Int], value: JsValue): Record = {
    val valueArray = value.asInstanceOf[JsArray]
    val values = valueArray.elements.map {
      case JsNumber(num) => num
      case JsString(str) => str
      case JsBoolean(boolean) => boolean
      case t => throw new MalformedResponseException("Found invalid type " + t.toString())
    }.toList

    new Record(namesIndex, values)
  }
}

abstract class QueryResponseException(message: String = null, throwable: Throwable = null) extends Exception(message, throwable)
class MalformedResponseException(message: String = null, throwable: Throwable = null)
  extends QueryResponseException(message, throwable)

class ErrorResponseException(message: String = null, throwable: Throwable = null)
  extends QueryResponseException(message, throwable)

class QueryResponse protected[influxdbclient] (val series: List[Series]) {}
object EmptyResponse
