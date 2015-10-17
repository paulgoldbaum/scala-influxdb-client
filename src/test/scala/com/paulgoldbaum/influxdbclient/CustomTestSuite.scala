package com.paulgoldbaum.influxdbclient

import org.scalatest.FunSuite

import scala.concurrent.duration._
import scala.concurrent.{Awaitable, Await}

class CustomTestSuite extends FunSuite {

  val waitDuration = 2.seconds

  def await[T](f: Awaitable[T], duration: Duration = waitDuration) = Await.result(f, duration)

  val influxdb = InfluxDB.connect()

}
