name := "fly-json-parser"

version := "0.1"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "org.apache.commons" % "commons-compress" % "1.14",
  "org.json4s" %% "json4s-jackson" % "3.2.11",
  "io.circe" %% "circe-parser" % "0.8.0")