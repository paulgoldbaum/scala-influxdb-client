package com.paulgoldbaum.influxdbclient

import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.concurrent.duration._
import scala.concurrent.{Await, Awaitable}

class CustomTestSuite extends FunSuite with BeforeAndAfterAll {

  val waitDuration = 2.seconds
  val databaseUsername = "influx_user"
  val databasePassword = "influx_password"

  val influxdb = InfluxDB.connect("localhost", 8086, databaseUsername, databasePassword, false)

  def await[T](f: Awaitable[T], duration: Duration = waitDuration) = Await.result(f, duration)

  override def afterAll = {
    influxdb.close()
  }

}
