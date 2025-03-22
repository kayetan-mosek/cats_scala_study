ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.6.4"

lazy val root = (project in file("."))
  .settings(
    name := "cats_scala_study",
    libraryDependencies ++= Seq(
      // "core" module - IO, IOApp, schedulers
      // This pulls in the kernel and std modules automatically.
      "org.typelevel" %% "cats-effect" % "3.5.7" withJavadoc() withSources(),
      // concurrency abstractions and primitives (Concurrent, Sync, Async etc.)
      "org.typelevel" %% "cats-effect-kernel" % "3.5.7",
      // standard "effect" library (Queues, Console, Random etc.)
      "org.typelevel" %% "cats-effect-std" % "3.5.7",
      "org.typelevel" %% "cats-effect-testing-specs2" % "1.6.0" % Test,
      "org.typelevel" %% "munit-cats-effect" % "2.0.0" % Test)
  )


