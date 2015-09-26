package com.paulgoldbaum.influxdbclient

sealed trait Field {
  def serialize: String
}

case class StringField(key: String, value: String) extends Field {
  def serialize = Util.escapeString(key) + "=\"" + value.replaceAll("\"", "\\\\\"") + "\""
}

case class DoubleField(key: String, value: Double) extends Field {
  def serialize = Util.escapeString(key) + "=" + value.toString
}

case class LongField(key: String, value: Long) extends Field {
  def serialize = Util.escapeString(key) + "=" + value.toString + "i"
}

case class BooleanField(key: String, value: Boolean) extends Field {
  def serialize = Util.escapeString(key) + "=" + (if(value) "t" else "f")
}
