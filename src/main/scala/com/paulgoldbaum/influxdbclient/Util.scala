package com.paulgoldbaum.influxdbclient

protected object Util {
 def escapeString(str: String) = str.replaceAll("([ ,=])", "\\\\$1")
}
