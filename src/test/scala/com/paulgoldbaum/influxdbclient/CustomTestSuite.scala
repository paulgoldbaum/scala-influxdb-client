package com.paulgoldbaum.influxdbclient

import org.scalatest.FunSuite

import scala.concurrent.duration._
import scala.concurrent.{Awaitable, Await}

class CustomTestSuite extends FunSuite {

  def await[T](f: Awaitable[T], duration: Duration = 1.second) = Await.result(f, duration)

}
