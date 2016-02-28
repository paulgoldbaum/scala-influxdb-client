package com.paulgoldbaum.influxdbclient

import spray.json._

class Record protected[influxdbclient]
(namesIndex: Map[String, Int], values: List[Any]) {
  def apply(position: Int) = values(position)
  def apply(name: String) = values(namesIndex(name))
  def allValues = values
}

class TagSet protected[influxdbclient]
(tagsIndex: Map[String, Int], values: List[Any]) {
  def apply(position: Int) = values(position)
  def apply(name: String) = values(tagsIndex(name))
  def size: Int = tagsIndex.size
}

class Series protected[influxdbclient]
(val name: String, val columns: List[String], val records: List[Record], val tags: TagSet) {
  def points(column: String) = records.map(_(column))
  def points(column: Int) = records.map(_(column))
  def allValues = records.map(_.allValues)
}

protected[influxdbclient]
object QueryResult {

  def fromJson(data: String): QueryResult = {
    val resultsArray = parseJson(data)
    val resultObject = resultsArray.elements.head.asInstanceOf[JsObject]
    makeSingleResult(resultObject)
  }

  def fromJsonMulti(data: String): List[QueryResult] = {
    val resultsArray = parseJson(data)
    resultsArray.elements.map(result => makeSingleResult(result.asInstanceOf[JsObject])).toList
  }

  private def makeSingleResult(resultObject: JsObject): QueryResult = {
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

  private def parseJson(data: String): JsArray = {
    val root = data.parseJson.asInstanceOf[JsObject]
    root.fields("results").asInstanceOf[JsArray]
  }

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

    val tagsIndex = if (fields.contains("tags"))
      fields("tags").asJsObject.fields.keySet.zipWithIndex.toMap
    else
      Map.empty[String, Int]

    val tags = if (fields.contains("tags"))
      constructTagSet(tagsIndex, fields("tags"))
    else
      constructTagSet(Map.empty[String, Int], JsObject.empty)

    val namesIndex = columns.zipWithIndex.toMap
    val records = if (fields.contains("values"))
      fields("values").asInstanceOf[JsArray].elements.map(constructRecord(namesIndex, _)).toList
    else
      List()
    new Series(seriesName, columns, records, tags)
  }

  def constructRecord(namesIndex: Map[String, Int], value: JsValue): Record = {
    val valueArray = value.asInstanceOf[JsArray]
    val values = valueArray.elements.map {
      case JsNumber(num) => num
      case JsString(str) => str
      case JsBoolean(boolean) => boolean
      case JsNull => null
      case t => throw new MalformedResponseException("Found invalid type " + t.toString())
    }.toList

    new Record(namesIndex, values)
  }

  def constructTagSet(tagsIndex: Map[String, Int], value: JsValue): TagSet = {
    val values = value.asJsObject.fields.map {
      case (key: String, JsNumber(num)) => num
      case (key: String, JsString(str)) => str
      case (key: String, JsBoolean(boolean)) => boolean
      case t => throw new MalformedResponseException("Found invalid type " + t.toString())
    }.toList

    new TagSet(tagsIndex, values)
  }
}

abstract class QueryResultException(message: String = null, throwable: Throwable = null)
  extends Exception(message, throwable)

class MalformedResponseException(message: String = null, throwable: Throwable = null)
  extends QueryResultException(message, throwable)

class ErrorResponseException(message: String = null, throwable: Throwable = null)
  extends QueryResultException(message, throwable)

class QueryResult protected[influxdbclient] (val series: List[Series] = List()) {}
