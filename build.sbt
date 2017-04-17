import io.gatling.sbt.GatlingPlugin

name := "Throttler"

version := "1.0"

scalaVersion := "2.11.8"

lazy val root = (project in file("."))
    .enablePlugins(GatlingPlugin)

lazy val akkaHttpV = "10.0.5"
lazy val gatlingV = "2.2.4"
libraryDependencies += "com.typesafe.akka" % "akka-http_2.11" % akkaHttpV
libraryDependencies += "com.typesafe.akka" % "akka-http-core_2.11" % akkaHttpV
libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingV % Test
libraryDependencies += "io.gatling"            % "gatling-test-framework"    % gatlingV % Test
libraryDependencies += "com.typesafe.akka" % "akka-http-testkit-experimental_2.11" % "2.4.2-RC3" % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % Test
