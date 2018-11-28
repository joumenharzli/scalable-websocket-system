

organization := "io.github.joumenharzli"
name := "notifications-dispatcher"
version := "0.1.0-SNAPSHOT"

licenses := Seq("GPLv3" -> url("https://www.gnu.org/licenses/gpl-3.0.txt"))
homepage := Some(url("https://github.com/joumenharzli/scalable-websocket-system"))

scalaVersion := "2.12.7"

val akkaVersion = "2.5.18"
val akkaServer = "10.1.5"
val kafkaClient = "2.0.0"
val scalaTest = "3.0.5"
val slf4j = "1.7.25"
val logback = "1.2.3"
val spray = "1.3.5"

resolvers += Resolver.bintrayRepo("cakesolutions", "maven")

libraryDependencies ++= Seq(

  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,

  "com.typesafe.akka" %% "akka-http" % akkaServer,

  "net.cakesolutions" %% "scala-kafka-client" % kafkaClient,
  "net.cakesolutions" %% "scala-kafka-client-akka" % kafkaClient exclude("com.typesafe.akka", "akka-actor"),

  "io.spray" %% "spray-json" % spray,

  "org.slf4j" % "slf4j-api" % slf4j,
  "ch.qos.logback" % "logback-classic" % logback,

  /* Test dependencies  */
  "com.typesafe.akka" %% "akka-http-testkit" % akkaServer % Test,
  "net.cakesolutions" %% "scala-kafka-client-testkit" % kafkaClient % Test,
  "org.scalatest" %% "scalatest" % scalaTest % Test

)

// Assembly config
lazy val app = (project in file(".")).
  settings(
    assemblyJarName in assembly := "notifications-dispatcher.jar",
    mainClass in assembly := Some("Application"),
    test in assembly := {}
  )