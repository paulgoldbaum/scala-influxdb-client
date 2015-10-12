name := "scala-influxdb-client"

organization := "com.paulgoldbaum.scala-influxdb-client"

version := "0.1"

scalaVersion := "2.11.7"
crossScalaVersions := Seq("2.11.7", "2.10.6")

testOptions in Test += Tests.Argument("-oDF")

libraryDependencies += "com.ning" % "async-http-client" % "1.9.31"
libraryDependencies += "io.netty" % "netty" % "3.10.4.Final"
libraryDependencies += "com.github.tomakehurst" % "wiremock" % "1.57" % "test"
libraryDependencies += "io.spray" %%  "spray-json" % "1.3.2"
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"
libraryDependencies += "com.github.tomakehurst" % "wiremock" % "1.57" % "test"
