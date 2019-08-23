package com.paulgoldbaum.influxdbclient

trait ToPoint[-A] {

  def convert(value: A): Point

}

object ToPoint {

  implicit object PointToPoint extends ToPoint[Point]{

    override def convert(value: Point): Point = value

  }

}
