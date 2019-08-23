package com.paulgoldbaum.influxdbclient

package object implicits {

  implicit class ToPointSyntax[A](val value: A) extends AnyVal {

    def toPoint(implicit toPoint: ToPoint[A]): Point = toPoint.convert(value)

  }

  implicit def anyToPoint[A](value: A)(implicit toPoint: ToPoint[A]): Point =
    toPoint.convert(value)

}
