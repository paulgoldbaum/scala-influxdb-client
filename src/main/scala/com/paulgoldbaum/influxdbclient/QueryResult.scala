package com.paulgoldbaum.influxdbclient

import spray.json._

class Record protected[influxdbclient]
(namesIndex: Map[String, Int], values: List[Any]) {
  def apply(position: Int) = values(position)
  def apply(name: String) = values(namesIndex(name))
}

class Series protected[influxdbclient]
(val name: String, val columns: List[String], val records: List[Record], val tags: Map[String, Any] = Map.empty[String, Any]) {
  def points(column: String) = records.map(_(column))
  def points(column: Int) = records.map(_(column))
}

object QueryResult {

  protected[influxdbclient]
  def fromJson(data: String): QueryResult = {
    val root = data.parseJson.asInstanceOf[JsObject]
    val resultsArray = root.fields("results").asInstanceOf[JsArray]
    val resultObject = resultsArray.elements.head.asInstanceOf[JsObject]

    val fields = resultObject.fields
    if (fields.contains("error")) {
      throw new ErrorResponseException(fields("error").toString())
    }

    if (!fields.contains("series")) {
      return new QueryResult()
    }
    val seriesArray = fields("series").asInstanceOf[JsArray]

    val series = seriesArray.elements.map(constructSeries).toList
    new QueryResult(series)
  }

  protected[influxdbclient]
  def constructSeries(value: JsValue): Series = {
    val fields = value.asInstanceOf[JsObject].fields
    val seriesName = if (fields.contains("name"))
      fields("name").asInstanceOf[JsString].value
    else
      ""

    val columns = fields("columns").asInstanceOf[JsArray].elements.map {
      case JsString(column) => column
      case t => throw new MalformedResponseException("Found invalid type " + t.toString())
    }.toList

    var tags = Map.empty[String, Any]
    if (fields.keySet.contains("tags")) {
      tags = fields("tags").asJsObject.fields.map {
        case (key: String, value: JsValue) =>
          value match {
            case JsNumber(num) => (key, num)
            case JsString(str) => (key, str)
            case JsBoolean(boolean) => (key, boolean)
          }
        case t => throw new MalformedResponseException("Found invalid type " + t.toString())
      }
    }

    val namesIndex = columns.zipWithIndex.toMap
    val records = if (fields.contains("values"))
      fields("values").asInstanceOf[JsArray].elements.map(constructRecord(namesIndex, _)).toList
    else
      List()
    new Series(seriesName, columns, records, tags)
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

abstract class QueryResultException(message: String = null, throwable: Throwable = null)
  extends Exception(message, throwable)

class MalformedResponseException(message: String = null, throwable: Throwable = null)
  extends QueryResultException(message, throwable)

class ErrorResponseException(message: String = null, throwable: Throwable = null)
  extends QueryResultException(message, throwable)

class QueryResult protected[influxdbclient] (val series: List[Series] = List()) {}
