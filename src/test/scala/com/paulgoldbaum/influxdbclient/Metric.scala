package com.paulgoldbaum.influxdbclient

/**
 * Class used for testing purposes.
 */
case class Metric(value: Int, tag: String)

object Metric {
  implicit val metricToPoint: ToPoint[Metric] = m =>
    Point("test_measurement").addField("value", m.value).addTag("tag_key", m.tag)
}