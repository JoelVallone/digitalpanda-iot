name := "iot-scala"

version := "1.0"

scalaVersion := "2.12.6"

lazy val akkaVersion = "2.6.0-M3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.datastax.cassandra" % "cassandra-driver-core" % "2.1.6",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)
