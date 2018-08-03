name := """play-scala-starter-example"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += Resolver.sonatypeRepo("snapshots")

scalaVersion := "2.12.4"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "com.h2database" % "h2" % "1.4.196"
libraryDependencies += "com.typesafe.play" %% "play-slick" % "3.0.0"
libraryDependencies +=  "com.chuusai" %% "shapeless" % "2.3.3"
libraryDependencies +=  "io.underscore" %% "slickless" % "0.3.3"
libraryDependencies += jdbc
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.41"
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.8.4"
libraryDependencies +=   "com.pauldijou" %% "jwt-play" % "0.16.0"
libraryDependencies += "com.github.t3hnar" %% "scala-bcrypt" % "3.0"
libraryDependencies += "org.apache.pdfbox" % "pdfbox" % "1.8.2"
libraryDependencies +=  "joda-time" % "joda-time" % "2.9.3"
libraryDependencies += "org.joda" % "joda-convert" % "1.2"



