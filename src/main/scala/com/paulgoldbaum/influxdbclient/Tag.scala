package com.paulgoldbaum.influxdbclient

case class Tag(key: String, value: String) {
  def serialize = Util.escapeString(key) + "=" + Util.escapeString(value)
}
