name := "crystal-ball-monocle"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.0"

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.1.0",
  "com.github.julien-truffaut"  %%  "monocle-core" % "1.0.1",
  "org.scalacheck" %% "scalacheck" % "1.12.1" % "test",
  "org.specs2" %% "specs2-core" % "2.4.15" % "test",
  "org.typelevel" %% "shapeless-scalacheck" % "0.3" % "test",
  "org.typelevel" %% "shapeless-scalaz" % "0.3" % "test"
)
