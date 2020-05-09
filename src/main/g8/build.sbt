import com.typesafe.sbt.SbtScalariform._

import scalariform.formatter.preferences._

name := """$name$"""
organization := "$organization$"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SwaggerPlugin)

scalaVersion := "2.13.1"

resolvers += Resolver.jcenterRepo

resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
resolvers += "Atlassian Releases" at "https://maven.atlassian.com/public/"

libraryDependencies += guice

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "5.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
  "mysql" % "mysql-connector-java" % "8.0.18"
)

libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette" % "6.1.0",
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "6.1.0",
  "com.mohiva" %% "play-silhouette-crypto-jca" % "6.1.0",
  "com.mohiva" %% "play-silhouette-persistence" % "6.1.0",
  "com.mohiva" %% "play-silhouette-testkit" % "6.1.0" % "test",
  "com.enragedginger" % "akka-quartz-scheduler_2.13" % "1.8.2-akka-2.6.x",
  "net.codingwell" %% "scala-guice" % "4.2.6",
  "com.iheart" %% "ficus" % "1.4.7",
  "com.zaxxer" % "HikariCP" % "3.4.2"
)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-mailer" % "8.0.0",
  "com.typesafe.play" %% "play-mailer-guice" % "8.0.0"
)

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % Test
libraryDependencies += "org.mockito" % "mockito-scala_2.13" % "1.7.1"

libraryDependencies ++= Seq(evolutions)

libraryDependencies += "org.webjars" % "swagger-ui" % "2.2.0"
swaggerV3 := true
swaggerPrettyJson := true
swaggerDomainNameSpaces := Seq("models")

//********************************************************
// Scalariform settings
//********************************************************

scalariformAutoformat := true

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(FormatXml, false)
  .setPreference(DoubleIndentConstructorArguments, false)
  .setPreference(DanglingCloseParenthesis, Preserve)
