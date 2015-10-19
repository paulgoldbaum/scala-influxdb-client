name := "scala-influxdb-client"

organization := "com.paulgoldbaum"

version := "0.2.0"

scalaVersion := "2.11.7"
crossScalaVersions := Seq("2.11.7", "2.10.6")

testOptions in Test += Tests.Argument("-oDF")

useGpg := true
releaseCrossBuild := true

libraryDependencies += "com.ning" % "async-http-client" % "1.9.31"
libraryDependencies += "io.netty" % "netty" % "3.10.4.Final"
libraryDependencies += "io.spray" %%  "spray-json" % "1.3.2"
libraryDependencies += "com.github.tomakehurst" % "wiremock" % "1.57" % "test"
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"
libraryDependencies += "com.github.tomakehurst" % "wiremock" % "1.57" % "test"

import ReleaseTransformations._

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(action = Command.process("publishSigned", _), enableCrossBuild = true),
  setNextVersion,
  commitNextVersion,
  ReleaseStep(action = Command.process("sonatypeReleaseAll", _), enableCrossBuild = true),
  pushChanges
)
