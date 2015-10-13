package com.paulgoldbaum.influxdbclient

import org.scalatest.FunSuite

import scala.concurrent.duration._
import scala.concurrent.{Awaitable, Await}

class CustomTestSuite extends FunSuite {

  def await[T](f: Awaitable[T], duration: Duration = 2.seconds) = Await.result(f, duration)

  val influxdb = InfluxDB.connect()

}
