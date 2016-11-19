package com.paulgoldbaum.influxdbclient

case class Tag(key: String, value: String) {
  require(value != null, "Tag values may not be null")
  require(!value.isEmpty, "Tag values may not be empty")

  def serialize = Util.escapeString(key) + "=" + Util.escapeString(value)
}
