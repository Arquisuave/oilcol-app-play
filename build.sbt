name := """play-java"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava,PlayEbean,LauncherJarPlugin)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  evolutions,
  javaJdbc,
  cache,
  javaWs,
  "org.postgresql" % "postgresql" % "9.4-1206-jdbc42",
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.8.1",
  "be.objectify" %% "deadbolt-scala" % "2.5.0",
  "be.objectify" %% "deadbolt-java" % "2.4.3"
)


// fork in run := true
libraryDependencies += "org.bouncycastle" % "bcprov-jdk15on" % "1.55"