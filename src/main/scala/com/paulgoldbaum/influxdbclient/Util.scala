package com.paulgoldbaum.influxdbclient

object Util {
 def escapeString(str: String) = str.replaceAll("([ ,=])", "\\\\$1")
}
