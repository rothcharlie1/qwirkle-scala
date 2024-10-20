ThisBuild / version := "1.0.0"

ThisBuild / scalaVersion := "2.13.12"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.10" % Test
libraryDependencies += "io.spray" %%  "spray-json" % "1.3.6"
libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.9.2"
libraryDependencies += "io.netty" % "netty-all" % "4.1.65.Final"


lazy val root = (project in file("."))
  .settings(
    name := "XServer",
    assembly / mainClass := Some("xserver.XServer")
  )

Compile / run / mainClass := Some("xserver.XServer")




