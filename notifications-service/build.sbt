/*
 * Copyright (C) 2018  Joumen Ali HARZLI
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

organization := "io.github.joumenharzli"
name := "notifications-service"
version := "0.1.0-SNAPSHOT"

organizationName := "Joumen Ali HARZLI"
startYear := Some(2018)
licenses := Seq("GPL-3.0" -> url("https://www.gnu.org/licenses/gpl-3.0.txt"))
homepage := Some(url("https://github.com/joumenharzli/scalable-websocket-system"))

scalaVersion := "2.12.7"

val kafkaClient   = "2.0.0"
val slf4j         = "1.7.25"
val phantom       = "2.29.0"
val macroParadise = "2.1.0"
val playTestPlus  = "3.1.2"
val cats          = "1.5.0"

resolvers += Resolver.bintrayRepo("cakesolutions", "maven")
resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= Seq(
  guice,
  "com.outworkers"    %% "phantom-dsl"             % phantom,
  "net.cakesolutions" %% "scala-kafka-client"      % kafkaClient,
  "net.cakesolutions" %% "scala-kafka-client-akka" % kafkaClient exclude ("com.typesafe.akka", "akka-actor"),
  "org.slf4j"         % "slf4j-api"                % slf4j,
  "org.typelevel"     %% "cats-core"               % cats,
  /* Test dependencies  */
  "org.scalatestplus.play" %% "scalatestplus-play"         % playTestPlus % Test,
  "net.cakesolutions"      %% "scala-kafka-client-testkit" % kafkaClient  % Test
)

addCompilerPlugin("org.scalamacros" % "paradise" % macroParadise cross CrossVersion.full)

// Assembly config
lazy val app = (project in file(".")).settings(
  assemblyJarName in assembly := "notifications-service.jar",
  mainClass in assembly := Some("Application"),
  test in assembly := {}
)

// Enable Scala plugin
enablePlugins(PlayScala)
enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
enablePlugins(AshScriptPlugin)

val dockerAppPath = "/app"
val logsPath      = dockerAppPath + "/logs"
val jreVersion    = "8u181"
defaultLinuxInstallLocation in Docker := dockerAppPath

dockerBaseImage := "openjdk:" + jreVersion + "-jre-alpine"
packageName in Docker := "joumenharzli/" + packageName.value
dockerUpdateLatest := true
dockerLabels := Map("maintainer" -> organizationName.value)
dockerEnvVars := Map("APP_DIR"   -> dockerAppPath)
dockerExposedPorts := Seq(8080)
dockerExposedVolumes := Seq(logsPath)
javaOptions in Universal ++= Seq("-Dconfig.resource=application-docker.conf",
                                 "-Dlogback.configurationFile=logback-docker.xml",
                                 "-Djava.security.egd=file:/dev/./urandom")
